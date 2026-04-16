package org.shyu.marketservicearbitration.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.shyu.marketcommon.exception.BusinessException;
import org.shyu.marketservicearbitration.dto.ArbitrationCompleteDTO;
import org.shyu.marketservicearbitration.dto.ArbitrationRejectDTO;
import org.shyu.marketservicearbitration.entity.ArbitrationEntity;
import org.shyu.marketservicearbitration.entity.ArbitrationExecutionTaskEntity;
import org.shyu.marketservicearbitration.entity.ArbitrationLogEntity;
import org.shyu.marketservicearbitration.entity.ArbitrationSupplementRequestEntity;
import org.shyu.marketservicearbitration.entity.DisputeRequestEntity;
import org.shyu.marketservicearbitration.enums.ArbitrationDecisionTypeEnum;
import org.shyu.marketservicearbitration.enums.ArbitrationExecutionStatusEnum;
import org.shyu.marketservicearbitration.enums.ArbitrationExecutionTypeEnum;
import org.shyu.marketservicearbitration.enums.DisputeStatusEnum;
import org.shyu.marketservicearbitration.service.IArbitrationDecisionWorkflowService;
import org.shyu.marketservicearbitration.service.IArbitrationExecutionService;
import org.shyu.marketservicearbitration.service.IArbitrationLogService;
import org.shyu.marketservicearbitration.service.IArbitrationService;
import org.shyu.marketservicearbitration.service.IArbitrationSupplementRequestService;
import org.shyu.marketservicearbitration.service.IDisputeService;
import org.shyu.marketservicearbitration.vo.AdminArbitrationDetailVO;
import org.shyu.marketservicearbitration.vo.AdminArbitrationListItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class ArbitrationDecisionWorkflowServiceImpl implements IArbitrationDecisionWorkflowService {

    private static final int PENDING = 0;
    private static final int PROCESSING = 1;
    private static final int COMPLETED = 2;
    private static final int REJECTED = 3;
    private static final int DECIDED = 5;

    private static final int SR_EXPIRED = 2;

    @Autowired
    private IArbitrationService arbitrationService;

    @Autowired
    private IArbitrationExecutionService arbitrationExecutionService;

    @Autowired
    private IArbitrationSupplementRequestService supplementRequestService;

    @Autowired
    private IArbitrationLogService arbitrationLogService;

    @Autowired
    private IDisputeService disputeService;

    @Override
    @Transactional
    public Boolean completeDecision(ArbitrationCompleteDTO dto, Long handlerId) {
        ArbitrationEntity arbitration = arbitrationService.getById(dto.getArbitrationId());
        if (arbitration == null) {
            throw new BusinessException("Arbitration record not found");
        }
        if (!Objects.equals(arbitration.getStatus(), PROCESSING)) {
            throw new BusinessException("Current status does not allow decision");
        }

        String decisionRemark = StringUtils.hasText(dto.getDecisionRemark()) ? dto.getDecisionRemark().trim() : "";
        if (!StringUtils.hasText(decisionRemark)) {
            throw new BusinessException("decisionRemark cannot be empty");
        }

        ArbitrationDecisionTypeEnum decisionType = ArbitrationDecisionTypeEnum.fromCode(dto.getDecisionType());
        ArbitrationExecutionTypeEnum executionType = mapExecutionType(decisionType);
        ArbitrationExecutionStatusEnum executionStatus = initialExecutionStatus(executionType);

        expirePendingSupplement(arbitration.getId(), handlerId, "Auto close pending supplement before decision");

        arbitration.setStatus(DECIDED);
        arbitration.setResult(buildDecisionSummary(decisionType, executionType, executionStatus, decisionRemark));
        arbitration.setDecisionRemark(decisionRemark);
        arbitration.setDecisionType(decisionType.getCode());
        arbitration.setExecutionType(executionType.getCode());
        arbitration.setExecutionStatus(executionStatus.getCode());
        arbitration.setExecutionRemark(StringUtils.hasText(dto.getExecutionRemark()) ? dto.getExecutionRemark().trim() : "");
        arbitration.setExecutionPayload(StringUtils.hasText(dto.getExecutionPayload()) ? dto.getExecutionPayload().trim() : null);
        arbitration.setDecideTime(LocalDateTime.now());
        arbitration.setExecuteTime(null);
        arbitration.setRejectReason(null);
        arbitration.setHandlerId(handlerId);
        arbitration.setUpdateTime(LocalDateTime.now());
        if (!arbitrationService.updateById(arbitration)) {
            throw new BusinessException("Failed to submit decision");
        }

        DisputeRequestEntity sourceDispute = arbitration.getSourceDisputeId() == null
                ? null
                : disputeService.getById(arbitration.getSourceDisputeId());
        if (sourceDispute != null) {
            sourceDispute.setStatus(DisputeStatusEnum.ARBITRATION_DECIDED.getCode());
            sourceDispute.setFinalDecisionType(decisionType.getCode());
            sourceDispute.setFinalExecutionStatus(executionStatus.getCode());
            sourceDispute.setFinalResultDescription(buildDecisionSummary(decisionType, executionType, executionStatus, decisionRemark));
            disputeService.updateById(sourceDispute);
        }

        recordLog(arbitration.getId(), handlerId, "DECIDE", "Admin decided: " + decisionType.getCode());
        ArbitrationExecutionTaskEntity task = arbitrationExecutionService.createExecutionTask(arbitration, sourceDispute);
        arbitrationExecutionService.executeTask(task.getId(), handlerId);
        return true;
    }

    @Override
    @Transactional
    public Boolean rejectDecision(ArbitrationRejectDTO dto, Long handlerId) {
        ArbitrationEntity arbitration = arbitrationService.getById(dto.getArbitrationId());
        if (arbitration == null) {
            throw new BusinessException("Arbitration record not found");
        }

        if (!Arrays.asList(PENDING, PROCESSING).contains(arbitration.getStatus())) {
            throw new BusinessException("Current status does not allow reject");
        }

        String rejectReason = StringUtils.hasText(dto.getRejectReason()) ? dto.getRejectReason().trim() : "";
        if (!StringUtils.hasText(rejectReason)) {
            throw new BusinessException("rejectReason cannot be empty");
        }

        expirePendingSupplement(arbitration.getId(), handlerId, "Auto close pending supplement before reject");

        arbitration.setStatus(REJECTED);
        arbitration.setResult("Rejected reason: " + rejectReason);
        arbitration.setDecisionRemark(null);
        arbitration.setDecisionType(ArbitrationDecisionTypeEnum.REJECT_BUYER_REQUEST.getCode());
        arbitration.setExecutionType(ArbitrationExecutionTypeEnum.CLOSE_DISPUTE.getCode());
        arbitration.setExecutionStatus(ArbitrationExecutionStatusEnum.NOT_REQUIRED.getCode());
        arbitration.setExecutionRemark("Decision rejected, no execution required");
        arbitration.setExecutionPayload(null);
        arbitration.setDecideTime(LocalDateTime.now());
        arbitration.setExecuteTime(LocalDateTime.now());
        arbitration.setRejectReason(rejectReason);
        arbitration.setHandlerId(handlerId);
        arbitration.setUpdateTime(LocalDateTime.now());
        if (!arbitrationService.updateById(arbitration)) {
            throw new BusinessException("Failed to reject arbitration");
        }

        DisputeRequestEntity sourceDispute = arbitration.getSourceDisputeId() == null
                ? null
                : disputeService.getById(arbitration.getSourceDisputeId());
        if (sourceDispute != null) {
            sourceDispute.setStatus(DisputeStatusEnum.CLOSED.getCode());
            sourceDispute.setFinalDecisionType(ArbitrationDecisionTypeEnum.REJECT_BUYER_REQUEST.getCode());
            sourceDispute.setFinalExecutionStatus(ArbitrationExecutionStatusEnum.NOT_REQUIRED.getCode());
            sourceDispute.setFinalResultDescription("Platform rejected this dispute request");
            disputeService.updateById(sourceDispute);
        }

        recordLog(arbitration.getId(), handlerId, "REJECT", "Admin rejected arbitration: " + rejectReason);
        recordLog(arbitration.getId(), handlerId, "CASE_ARCHIVED", "Case archived as rejected");
        return true;
    }

    @Override
    public void enrichAdminDetail(AdminArbitrationDetailVO detail, ArbitrationEntity entity) {
        if (detail == null || entity == null) {
            return;
        }
        detail.setStatus(entity.getStatus());
        detail.setStatusLabel(statusLabel(entity.getStatus()));
        detail.setDecisionType(entity.getDecisionType());
        detail.setDecisionTypeLabel(ArbitrationDecisionTypeEnum.labelOf(entity.getDecisionType()));
        detail.setExecutionType(entity.getExecutionType());
        detail.setExecutionTypeLabel(ArbitrationExecutionTypeEnum.labelOf(entity.getExecutionType()));
        detail.setExecutionStatus(entity.getExecutionStatus());
        detail.setExecutionStatusLabel(ArbitrationExecutionStatusEnum.labelOf(entity.getExecutionStatus()));
        detail.setExecutionRemark(entity.getExecutionRemark());
        detail.setExecutionPayload(entity.getExecutionPayload());
        detail.setDecideTime(entity.getDecideTime());
        detail.setExecuteTime(entity.getExecuteTime());

        if (Objects.equals(entity.getStatus(), DECIDED) || Objects.equals(entity.getStatus(), COMPLETED)) {
            if (!StringUtils.hasText(detail.getDecisionRemark())) {
                detail.setDecisionRemark(StringUtils.hasText(entity.getDecisionRemark()) ? entity.getDecisionRemark() : entity.getResult());
            }
        }

        detail.setCanAccept(Objects.equals(entity.getStatus(), PENDING));
        detail.setCanComplete(Objects.equals(entity.getStatus(), PROCESSING));
        detail.setCanReject(Arrays.asList(PENDING, PROCESSING).contains(entity.getStatus()));
        detail.setReadonly(Arrays.asList(DECIDED, COMPLETED, REJECTED).contains(entity.getStatus()));
    }

    @Override
    public void enrichAdminListItem(AdminArbitrationListItemVO item, ArbitrationEntity entity) {
        if (item == null || entity == null) {
            return;
        }
        item.setStatus(entity.getStatus());
        item.setStatusLabel(statusLabel(entity.getStatus()));
        item.setDecisionType(entity.getDecisionType());
        item.setDecisionTypeLabel(ArbitrationDecisionTypeEnum.labelOf(entity.getDecisionType()));
        item.setExecutionType(entity.getExecutionType());
        item.setExecutionTypeLabel(ArbitrationExecutionTypeEnum.labelOf(entity.getExecutionType()));
        item.setExecutionStatus(entity.getExecutionStatus());
        item.setExecutionStatusLabel(ArbitrationExecutionStatusEnum.labelOf(entity.getExecutionStatus()));
    }

    private void expirePendingSupplement(Long arbitrationId, Long operatorId, String remarkPrefix) {
        List<ArbitrationSupplementRequestEntity> pending = supplementRequestService.listPendingByArbitrationId(arbitrationId);
        for (ArbitrationSupplementRequestEntity req : pending) {
            req.setStatus(SR_EXPIRED);
            req.setUpdateTime(LocalDateTime.now());
            supplementRequestService.updateById(req);
            recordLog(arbitrationId, operatorId, "SUPPLEMENT_EXPIRED", remarkPrefix + ", requestId=" + req.getId());
        }
    }

    private void recordLog(Long arbitrationId, Long operatorId, String action, String remark) {
        ArbitrationLogEntity logEntity = new ArbitrationLogEntity();
        logEntity.setArbitrationId(arbitrationId);
        logEntity.setOperatorId(operatorId == null ? 0L : operatorId);
        logEntity.setAction(action);
        logEntity.setRemark(remark);
        arbitrationLogService.save(logEntity);
    }

    private ArbitrationExecutionTypeEnum mapExecutionType(ArbitrationDecisionTypeEnum decisionType) {
        if (decisionType == null) {
            return ArbitrationExecutionTypeEnum.WAIT_MANUAL_OFFLINE;
        }
        switch (decisionType) {
            case SUPPORT_FULL_REFUND:
                return ArbitrationExecutionTypeEnum.REFUND_FULL;
            case SUPPORT_PARTIAL_REFUND:
                return ArbitrationExecutionTypeEnum.REFUND_PARTIAL;
            case SUPPORT_RETURN_AND_REFUND:
                return ArbitrationExecutionTypeEnum.RETURN_FLOW;
            case SUPPORT_REPLACE:
                return ArbitrationExecutionTypeEnum.REPLACE_FLOW;
            case REJECT_BUYER_REQUEST:
                return ArbitrationExecutionTypeEnum.CLOSE_DISPUTE;
            case CLOSE_WITH_NEGOTIATION_RESULT:
                return ArbitrationExecutionTypeEnum.NO_ACTION;
            case REQUIRE_SUPPLEMENT:
            case OTHER:
            default:
                return ArbitrationExecutionTypeEnum.WAIT_MANUAL_OFFLINE;
        }
    }

    private ArbitrationExecutionStatusEnum initialExecutionStatus(ArbitrationExecutionTypeEnum executionType) {
        if (executionType == null) {
            return ArbitrationExecutionStatusEnum.MANUAL_REQUIRED;
        }
        switch (executionType) {
            case NO_ACTION:
            case CLOSE_DISPUTE:
                return ArbitrationExecutionStatusEnum.NOT_REQUIRED;
            case WAIT_MANUAL_OFFLINE:
                return ArbitrationExecutionStatusEnum.MANUAL_REQUIRED;
            default:
                return ArbitrationExecutionStatusEnum.PENDING;
        }
    }

    private String statusLabel(Integer status) {
        if (Objects.equals(status, PENDING)) return "待处理";
        if (Objects.equals(status, PROCESSING)) return "处理中";
        if (Objects.equals(status, DECIDED)) return "已裁决";
        if (Objects.equals(status, COMPLETED)) return "已执行完成";
        if (Objects.equals(status, REJECTED)) return "已驳回";
        return "未知";
    }

    private String buildDecisionSummary(ArbitrationDecisionTypeEnum decisionType,
                                        ArbitrationExecutionTypeEnum executionType,
                                        ArbitrationExecutionStatusEnum executionStatus,
                                        String decisionRemark) {
        return "裁决结果: " + decisionType.getLabel()
                + "；执行动作: " + executionType.getLabel()
                + "；执行状态: " + executionStatus.getLabel()
                + "；说明: " + decisionRemark;
    }
}
