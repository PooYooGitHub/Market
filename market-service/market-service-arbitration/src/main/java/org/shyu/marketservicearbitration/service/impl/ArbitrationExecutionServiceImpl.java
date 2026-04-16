package org.shyu.marketservicearbitration.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.shyu.marketcommon.exception.BusinessException;
import org.shyu.marketservicearbitration.entity.ArbitrationEntity;
import org.shyu.marketservicearbitration.entity.ArbitrationExecutionTaskEntity;
import org.shyu.marketservicearbitration.entity.ArbitrationLogEntity;
import org.shyu.marketservicearbitration.entity.DisputeRequestEntity;
import org.shyu.marketservicearbitration.enums.ArbitrationExecutionStatusEnum;
import org.shyu.marketservicearbitration.enums.ArbitrationExecutionTypeEnum;
import org.shyu.marketservicearbitration.enums.DisputeStatusEnum;
import org.shyu.marketservicearbitration.mapper.ArbitrationMapper;
import org.shyu.marketservicearbitration.mapper.DisputeRequestMapper;
import org.shyu.marketservicearbitration.service.IArbitrationExecutionService;
import org.shyu.marketservicearbitration.service.IArbitrationExecutionTaskService;
import org.shyu.marketservicearbitration.service.IArbitrationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
public class ArbitrationExecutionServiceImpl implements IArbitrationExecutionService {

    private static final int ARBITRATION_COMPLETED = 2;
    private static final int ARBITRATION_DECIDED = 5;

    @Autowired
    private IArbitrationExecutionTaskService executionTaskService;

    @Autowired
    private ArbitrationMapper arbitrationMapper;

    @Autowired
    private DisputeRequestMapper disputeRequestMapper;

    @Autowired
    private IArbitrationLogService arbitrationLogService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public ArbitrationExecutionTaskEntity createExecutionTask(ArbitrationEntity arbitration, DisputeRequestEntity dispute) {
        if (arbitration == null || arbitration.getId() == null) {
            throw new BusinessException("Arbitration record does not exist");
        }

        ArbitrationExecutionTaskEntity task = new ArbitrationExecutionTaskEntity();
        task.setArbitrationId(arbitration.getId());
        task.setDisputeId(dispute == null ? null : dispute.getId());
        task.setOrderId(arbitration.getOrderId());
        task.setExecutionType(arbitration.getExecutionType());
        task.setExecutionStatus(StringUtils.hasText(arbitration.getExecutionStatus())
                ? arbitration.getExecutionStatus()
                : ArbitrationExecutionStatusEnum.PENDING.getCode());
        task.setPayload(buildTaskPayload(arbitration, dispute));
        task.setResultMessage("Execution task created");
        task.setRetryCount(0);
        executionTaskService.save(task);

        recordLog(arbitration.getId(), arbitration.getHandlerId(), "EXECUTION_TASK_CREATED",
                "Execution task created, taskId=" + task.getId() + ", type=" + task.getExecutionType());
        return task;
    }

    @Override
    @Transactional
    public void executeTask(Long taskId, Long operatorId) {
        ArbitrationExecutionTaskEntity task = executionTaskService.getById(taskId);
        if (task == null) {
            throw new BusinessException("Execution task does not exist");
        }

        ArbitrationEntity arbitration = arbitrationMapper.selectById(task.getArbitrationId());
        if (arbitration == null) {
            throw new BusinessException("Arbitration record does not exist");
        }

        DisputeRequestEntity dispute = null;
        if (task.getDisputeId() != null) {
            dispute = disputeRequestMapper.selectById(task.getDisputeId());
        } else if (arbitration.getSourceDisputeId() != null) {
            dispute = disputeRequestMapper.selectById(arbitration.getSourceDisputeId());
        }

        ArbitrationExecutionTypeEnum executionType = ArbitrationExecutionTypeEnum.fromCode(task.getExecutionType());
        recordLog(arbitration.getId(), operatorId, "EXECUTION_START",
                "Execution started, taskId=" + task.getId() + ", type=" + executionType.getCode());

        switch (executionType) {
            case REFUND_FULL:
                handleRefundFull(task, arbitration, dispute, operatorId);
                break;
            case REFUND_PARTIAL:
                handleRefundPartial(task, arbitration, dispute, operatorId);
                break;
            case RETURN_FLOW:
                handleReturnFlow(task, arbitration, dispute, operatorId);
                break;
            case REPLACE_FLOW:
                handleReplaceFlow(task, arbitration, dispute, operatorId);
                break;
            case CLOSE_DISPUTE:
            case NO_ACTION:
                handleCloseDispute(task, arbitration, dispute, operatorId);
                break;
            case WAIT_MANUAL_OFFLINE:
                markManualRequired(task, arbitration, dispute, operatorId,
                        "Manual offline handling required");
                break;
            default:
                markExecutionFailed(task, "Unsupported execution type: " + task.getExecutionType(), operatorId);
                break;
        }
    }

    @Override
    @Transactional
    public void markExecutionSuccess(ArbitrationExecutionTaskEntity task, String message, Long operatorId) {
        LocalDateTime now = LocalDateTime.now();

        task.setExecutionStatus(ArbitrationExecutionStatusEnum.SUCCESS.getCode());
        task.setResultMessage(message);
        task.setFinishTime(now);
        executionTaskService.updateById(task);

        ArbitrationEntity arbitration = arbitrationMapper.selectById(task.getArbitrationId());
        if (arbitration != null) {
            arbitration.setExecutionStatus(ArbitrationExecutionStatusEnum.SUCCESS.getCode());
            arbitration.setExecutionRemark(message);
            arbitration.setExecuteTime(now);
            arbitration.setStatus(ARBITRATION_COMPLETED);
            arbitration.setUpdateTime(now);
            arbitrationMapper.updateById(arbitration);
        }

        if (task.getDisputeId() != null) {
            DisputeRequestEntity dispute = disputeRequestMapper.selectById(task.getDisputeId());
            if (dispute != null) {
                if (!DisputeStatusEnum.CLOSED.getCode().equals(dispute.getStatus())) {
                    dispute.setStatus(DisputeStatusEnum.ARBITRATION_EXECUTED.getCode());
                }
                dispute.setFinalExecutionStatus(ArbitrationExecutionStatusEnum.SUCCESS.getCode());
                dispute.setFinalResultDescription(message);
                disputeRequestMapper.updateById(dispute);
            }
        }

        recordLog(task.getArbitrationId(), operatorId, "EXECUTION_SUCCESS", message);
        recordLog(task.getArbitrationId(), operatorId, "CASE_ARCHIVED", "Case archived after execution success");
    }

    @Override
    @Transactional
    public void markExecutionFailed(ArbitrationExecutionTaskEntity task, String message, Long operatorId) {
        LocalDateTime now = LocalDateTime.now();

        task.setExecutionStatus(ArbitrationExecutionStatusEnum.FAILED.getCode());
        task.setResultMessage(message);
        task.setRetryCount((task.getRetryCount() == null ? 0 : task.getRetryCount()) + 1);
        task.setFinishTime(now);
        executionTaskService.updateById(task);

        ArbitrationEntity arbitration = arbitrationMapper.selectById(task.getArbitrationId());
        if (arbitration != null) {
            arbitration.setExecutionStatus(ArbitrationExecutionStatusEnum.FAILED.getCode());
            arbitration.setExecutionRemark(message);
            arbitration.setStatus(ARBITRATION_DECIDED);
            arbitration.setUpdateTime(now);
            arbitrationMapper.updateById(arbitration);
        }

        if (task.getDisputeId() != null) {
            DisputeRequestEntity dispute = disputeRequestMapper.selectById(task.getDisputeId());
            if (dispute != null) {
                dispute.setStatus(DisputeStatusEnum.ARBITRATION_EXECUTING.getCode());
                dispute.setFinalExecutionStatus(ArbitrationExecutionStatusEnum.FAILED.getCode());
                dispute.setFinalResultDescription(message);
                disputeRequestMapper.updateById(dispute);
            }
        }

        recordLog(task.getArbitrationId(), operatorId, "EXECUTION_FAILED", message);
    }

    @Override
    @Transactional
    public void handleRefundFull(ArbitrationExecutionTaskEntity task, ArbitrationEntity arbitration,
                                 DisputeRequestEntity dispute, Long operatorId) {
        markProcessing(task, arbitration, dispute,
                "Decision supports full refund. Waiting for refund channel integration.", operatorId);
    }

    @Override
    @Transactional
    public void handleRefundPartial(ArbitrationExecutionTaskEntity task, ArbitrationEntity arbitration,
                                    DisputeRequestEntity dispute, Long operatorId) {
        markProcessing(task, arbitration, dispute,
                "Decision supports partial refund. Waiting for refund channel integration.", operatorId);
    }

    @Override
    @Transactional
    public void handleReturnFlow(ArbitrationExecutionTaskEntity task, ArbitrationEntity arbitration,
                                 DisputeRequestEntity dispute, Long operatorId) {
        markProcessing(task, arbitration, dispute,
                "Decision supports return and refund. Buyer should return goods first.", operatorId);
    }

    @Override
    @Transactional
    public void handleCloseDispute(ArbitrationExecutionTaskEntity task, ArbitrationEntity arbitration,
                                   DisputeRequestEntity dispute, Long operatorId) {
        String message = "Decision closes dispute. No automatic execution required.";
        if (dispute != null) {
            dispute.setStatus(DisputeStatusEnum.CLOSED.getCode());
            disputeRequestMapper.updateById(dispute);
        }
        markExecutionSuccess(task, message, operatorId);
    }

    private void handleReplaceFlow(ArbitrationExecutionTaskEntity task, ArbitrationEntity arbitration,
                                   DisputeRequestEntity dispute, Long operatorId) {
        markProcessing(task, arbitration, dispute,
                "Decision supports replacement flow. Waiting for manual after-sale coordination.", operatorId);
    }

    private void markManualRequired(ArbitrationExecutionTaskEntity task, ArbitrationEntity arbitration,
                                    DisputeRequestEntity dispute, Long operatorId, String message) {
        LocalDateTime now = LocalDateTime.now();

        task.setExecutionStatus(ArbitrationExecutionStatusEnum.MANUAL_REQUIRED.getCode());
        task.setResultMessage(message);
        task.setFinishTime(now);
        executionTaskService.updateById(task);

        arbitration.setExecutionStatus(ArbitrationExecutionStatusEnum.MANUAL_REQUIRED.getCode());
        arbitration.setExecutionRemark(message);
        arbitration.setStatus(ARBITRATION_DECIDED);
        arbitration.setUpdateTime(now);
        arbitrationMapper.updateById(arbitration);

        if (dispute != null) {
            dispute.setStatus(DisputeStatusEnum.ARBITRATION_EXECUTING.getCode());
            dispute.setFinalExecutionStatus(ArbitrationExecutionStatusEnum.MANUAL_REQUIRED.getCode());
            dispute.setFinalResultDescription(message);
            disputeRequestMapper.updateById(dispute);
        }

        recordLog(arbitration.getId(), operatorId, "EXECUTION_MANUAL_REQUIRED", message);
    }

    private void markProcessing(ArbitrationExecutionTaskEntity task, ArbitrationEntity arbitration,
                                DisputeRequestEntity dispute, String message, Long operatorId) {
        LocalDateTime now = LocalDateTime.now();

        task.setExecutionStatus(ArbitrationExecutionStatusEnum.PROCESSING.getCode());
        task.setResultMessage(message);
        task.setUpdateTime(now);
        executionTaskService.updateById(task);

        arbitration.setExecutionStatus(ArbitrationExecutionStatusEnum.PROCESSING.getCode());
        arbitration.setExecutionRemark(message);
        arbitration.setStatus(ARBITRATION_DECIDED);
        arbitration.setUpdateTime(now);
        arbitrationMapper.updateById(arbitration);

        if (dispute != null) {
            dispute.setStatus(DisputeStatusEnum.ARBITRATION_EXECUTING.getCode());
            dispute.setFinalExecutionStatus(ArbitrationExecutionStatusEnum.PROCESSING.getCode());
            dispute.setFinalResultDescription(message);
            disputeRequestMapper.updateById(dispute);
        }

        recordLog(arbitration.getId(), operatorId, "EXECUTION_PROCESSING", message);
    }

    private String buildTaskPayload(ArbitrationEntity arbitration, DisputeRequestEntity dispute) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("arbitrationId", arbitration.getId());
        payload.put("disputeId", dispute == null ? arbitration.getSourceDisputeId() : dispute.getId());
        payload.put("orderId", arbitration.getOrderId());
        payload.put("decisionType", arbitration.getDecisionType());
        payload.put("executionType", arbitration.getExecutionType());
        payload.put("expectedAmount", arbitration.getExpectedAmount());
        payload.put("requestType", arbitration.getRequestType());
        payload.put("requestDescription", arbitration.getRequestDescription());
        payload.put("decisionRemark", arbitration.getDecisionRemark());
        if (StringUtils.hasText(arbitration.getExecutionPayload())) {
            payload.put("extraPayload", arbitration.getExecutionPayload());
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            log.warn("serialize execution payload failed, arbitrationId={}", arbitration.getId(), ex);
            return "{}";
        }
    }

    private void recordLog(Long arbitrationId, Long operatorId, String action, String remark) {
        if (arbitrationId == null) {
            return;
        }
        ArbitrationLogEntity logEntity = new ArbitrationLogEntity();
        logEntity.setArbitrationId(arbitrationId);
        logEntity.setOperatorId(operatorId == null ? 0L : operatorId);
        logEntity.setAction(action);
        logEntity.setRemark(remark);
        arbitrationLogService.save(logEntity);
    }
}
