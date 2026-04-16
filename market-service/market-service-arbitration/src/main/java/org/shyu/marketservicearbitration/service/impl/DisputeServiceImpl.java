package org.shyu.marketservicearbitration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.shyu.marketapimessage.dto.MessageChatHistoryPageDTO;
import org.shyu.marketapimessage.dto.MessageChatRecordDTO;
import org.shyu.marketapimessage.feign.MessageFeignClient;
import org.shyu.marketapitrade.dto.OrderDTO;
import org.shyu.marketapitrade.feign.TradeFeignClient;
import org.shyu.marketcommon.exception.BusinessException;
import org.shyu.marketcommon.result.Result;
import org.shyu.marketservicearbitration.dto.BuyerConfirmProposalDTO;
import org.shyu.marketservicearbitration.dto.DisputeCreateDTO;
import org.shyu.marketservicearbitration.dto.DisputeEscalateDTO;
import org.shyu.marketservicearbitration.dto.SellerDisputeResponseDTO;
import org.shyu.marketservicearbitration.entity.ArbitrationEntity;
import org.shyu.marketservicearbitration.entity.ArbitrationEvidenceSubmissionEntity;
import org.shyu.marketservicearbitration.entity.ArbitrationLogEntity;
import org.shyu.marketservicearbitration.entity.DisputeEvidenceEntity;
import org.shyu.marketservicearbitration.entity.DisputeNegotiationLogEntity;
import org.shyu.marketservicearbitration.entity.DisputeRequestEntity;
import org.shyu.marketservicearbitration.enums.ArbitrationExecutionStatusEnum;
import org.shyu.marketservicearbitration.enums.DisputeActorRoleEnum;
import org.shyu.marketservicearbitration.enums.DisputeNegotiationActionEnum;
import org.shyu.marketservicearbitration.enums.DisputeStatusEnum;
import org.shyu.marketservicearbitration.enums.SellerResponseTypeEnum;
import org.shyu.marketservicearbitration.mapper.ArbitrationMapper;
import org.shyu.marketservicearbitration.mapper.DisputeEvidenceMapper;
import org.shyu.marketservicearbitration.mapper.DisputeNegotiationLogMapper;
import org.shyu.marketservicearbitration.mapper.DisputeRequestMapper;
import org.shyu.marketservicearbitration.service.IArbitrationEvidenceSubmissionService;
import org.shyu.marketservicearbitration.service.IArbitrationLogService;
import org.shyu.marketservicearbitration.service.IDisputeService;
import org.shyu.marketservicearbitration.vo.DisputeChatSummaryVO;
import org.shyu.marketservicearbitration.vo.DisputeDetailVO;
import org.shyu.marketservicearbitration.vo.DisputeEvidenceVO;
import org.shyu.marketservicearbitration.vo.DisputeListItemVO;
import org.shyu.marketservicearbitration.vo.DisputeNegotiationLogVO;
import org.shyu.marketservicearbitration.vo.SellerProposalVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DisputeServiceImpl extends ServiceImpl<DisputeRequestMapper, DisputeRequestEntity>
        implements IDisputeService {

    private static final int ARBITRATION_PENDING = 0;
    private static final int SELLER_RESPONSE_TIMEOUT_HOURS = 24;
    private static final int BUYER_CONFIRM_TIMEOUT_HOURS = 24;
    private static final int CHAT_PAGE_SIZE = 50;
    private static final int CHAT_MAX_PAGES = 6;
    private static final int CHAT_RESULT_LIMIT = 20;
    private static final BigDecimal HIGH_AMOUNT_ESCALATE_THRESHOLD = new BigDecimal("5000.00");
    private static final String EVIDENCE_BIZ_DISPUTE = "DISPUTE";
    private static final String EVIDENCE_BIZ_ARBITRATION = "ARBITRATION";
    private static final String EXECUTION_STATUS_FULL_REFUND_ACCEPTED = "FULL_REFUND_ACCEPTED";
    private static final String EXECUTION_STATUS_PARTIAL_REFUND_ACCEPTED = "PARTIAL_REFUND_ACCEPTED";
    private static final String EXECUTION_STATUS_RETURN_AND_REFUND_PENDING = "RETURN_AND_REFUND_PENDING";
    private static final String EXECUTION_STATUS_NEGOTIATION_SUCCESS = "NEGOTIATION_SUCCESS";
    private static final String EXECUTION_STATUS_NOT_APPLICABLE = "NOT_APPLICABLE";

    private static final List<String> DISPUTE_CHAT_KEYWORDS = Arrays.asList(
            "功能正常", "无瑕疵", "已说明", "退款", "退货", "运费", "损坏", "描述不符", "协商", "同意", "拒绝"
    );

    @Autowired
    private TradeFeignClient tradeFeignClient;

    @Autowired
    private MessageFeignClient messageFeignClient;

    @Autowired
    private DisputeEvidenceMapper disputeEvidenceMapper;

    @Autowired
    private DisputeNegotiationLogMapper disputeNegotiationLogMapper;

    @Autowired
    private ArbitrationMapper arbitrationMapper;

    @Autowired
    private IArbitrationLogService arbitrationLogService;

    @Autowired
    private IArbitrationEvidenceSubmissionService arbitrationEvidenceSubmissionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public Long createDispute(DisputeCreateDTO dto, Long buyerId) {
        checkAndMarkTimeout();
        OrderDTO orderDTO = fetchAndValidateOrder(dto.getOrderId(), buyerId);
        ensureNoActiveDispute(dto.getOrderId(), buyerId);

        DisputeRequestEntity dispute = new DisputeRequestEntity();
        dispute.setOrderId(orderDTO.getId());
        dispute.setProductId(dto.getProductId() != null ? dto.getProductId() : orderDTO.getProductId());
        dispute.setBuyerId(orderDTO.getBuyerId());
        dispute.setSellerId(orderDTO.getSellerId());
        dispute.setReason(dto.getReason().trim());
        dispute.setFactDescription(dto.getFactDescription().trim());
        dispute.setRequestType(dto.getRequestType().trim().toUpperCase(Locale.ROOT));
        dispute.setRequestDescription(dto.getRequestDescription().trim());
        dispute.setExpectedAmount(dto.getExpectedAmount() == null ? BigDecimal.ZERO : dto.getExpectedAmount());
        dispute.setFinalDecisionType(null);
        dispute.setFinalExecutionStatus(null);
        dispute.setFinalResultDescription(null);
        dispute.setStatus(DisputeStatusEnum.WAIT_SELLER_RESPONSE.getCode());
        dispute.setCurrentRound(1);
        dispute.setExpireTime(LocalDateTime.now().plusHours(SELLER_RESPONSE_TIMEOUT_HOURS));
        if (!save(dispute)) {
            throw new BusinessException("创建争议申请失败");
        }

        saveDisputeEvidence(dispute.getId(), dto.getEvidenceList(), buyerId, DisputeActorRoleEnum.BUYER.getCode());
        recordLog(dispute.getId(), dispute.getCurrentRound(), buyerId, DisputeActorRoleEnum.BUYER.getCode(),
                DisputeNegotiationActionEnum.BUYER_CREATE.getCode(), "买家发起争议：" + dispute.getFactDescription(), dispute.getExpectedAmount());

        if (hitHighRiskRule(dispute)) {
            dispute.setStatus(DisputeStatusEnum.NEGOTIATION_FAILED.getCode());
            dispute.setExpireTime(null);
            updateById(dispute);
            recordLog(dispute.getId(), dispute.getCurrentRound(), 0L, DisputeActorRoleEnum.SYSTEM.getCode(),
                    DisputeNegotiationActionEnum.SYSTEM_AUTO_EXECUTE.getCode(),
                    "命中高金额规则，建议直接升级仲裁", dispute.getExpectedAmount());
        }
        return dispute.getId();
    }

    @Override
    public IPage<DisputeListItemVO> getBuyerDisputeList(Long buyerId, Integer current, Integer size) {
        checkAndMarkTimeout();
        Page<DisputeRequestEntity> page = new Page<>(current, size);
        QueryWrapper<DisputeRequestEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("buyer_id", buyerId).orderByDesc("create_time");
        IPage<DisputeRequestEntity> entityPage = page(page, wrapper);
        return toListItemPage(entityPage);
    }

    @Override
    public DisputeListItemVO getMyDisputeByOrderId(Long buyerId, Long orderId) {
        checkAndMarkTimeout();
        QueryWrapper<DisputeRequestEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("buyer_id", buyerId)
                .eq("order_id", orderId)
                .orderByDesc("create_time")
                .last("limit 1");
        DisputeRequestEntity entity = getOne(wrapper, false);
        if (entity == null) {
            return null;
        }
        refreshSingleTimeout(entity.getId());
        DisputeRequestEntity latest = getById(entity.getId());
        return latest == null ? null : toListItemVO(latest);
    }

    @Override
    public IPage<DisputeListItemVO> getSellerPendingDisputes(Long sellerId, Integer current, Integer size) {
        checkAndMarkTimeout();
        Page<DisputeRequestEntity> page = new Page<>(current, size);
        QueryWrapper<DisputeRequestEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("seller_id", sellerId)
                .in("status", Arrays.asList(
                        DisputeStatusEnum.WAIT_SELLER_RESPONSE.getCode(),
                        DisputeStatusEnum.SELLER_TIMEOUT.getCode()))
                .orderByAsc("expire_time")
                .orderByDesc("create_time");
        IPage<DisputeRequestEntity> entityPage = page(page, wrapper);
        return toListItemPage(entityPage);
    }

    @Override
    public DisputeDetailVO getDisputeDetail(Long disputeId, Long currentUserId) {
        refreshSingleTimeout(disputeId);
        DisputeRequestEntity dispute = getById(disputeId);
        if (dispute == null) {
            throw new BusinessException("争议不存在");
        }

        List<DisputeEvidenceEntity> evidenceEntities = listDisputeEvidence(disputeId);
        List<DisputeNegotiationLogEntity> logEntities = listNegotiationLog(disputeId);
        logEntities.sort(Comparator.comparing(DisputeNegotiationLogEntity::getCreateTime,
                Comparator.nullsLast(Comparator.naturalOrder())));

        DisputeDetailVO vo = new DisputeDetailVO();
        BeanUtils.copyProperties(dispute, vo);
        vo.setStatusLabel(DisputeStatusEnum.getLabel(dispute.getStatus()));
        vo.setCountdownSeconds(calcCountdownSeconds(dispute.getExpireTime()));
        vo.setEvidenceList(evidenceEntities.stream().map(this::toEvidenceVO).collect(Collectors.toList()));
        vo.setNegotiationLogs(logEntities.stream().map(this::toLogVO).collect(Collectors.toList()));
        vo.setNegotiationSummary(buildNegotiationSummary(disputeId));
        vo.setChatSummary(buildChatSummaryByOrder(dispute.getOrderId(), dispute.getBuyerId(), dispute.getSellerId(),
                dispute.getCreateTime(), LocalDateTime.now()));
        vo.setSellerProposal(buildSellerProposal(dispute));
        vo.setCanSellerRespond(DisputeStatusEnum.canSellerRespond(dispute.getStatus()));
        vo.setCanBuyerConfirm(DisputeStatusEnum.WAIT_BUYER_CONFIRM.getCode().equals(dispute.getStatus()));
        vo.setCanEscalate(DisputeStatusEnum.canEscalate(dispute.getStatus()));

        NegotiationExecution execution = resolveNegotiationExecution(dispute);
        if (StringUtils.hasText(dispute.getFinalExecutionStatus())) {
            vo.setExecutionStatus(dispute.getFinalExecutionStatus());
            vo.setExecutionStatusLabel(ArbitrationExecutionStatusEnum.labelOf(dispute.getFinalExecutionStatus()));
            vo.setExecutionRemark(firstText(dispute.getFinalResultDescription(), execution.getRemark()));
        } else {
            vo.setExecutionStatus(execution.getCode());
            vo.setExecutionStatusLabel(execution.getLabel());
            vo.setExecutionRemark(execution.getRemark());
        }
        vo.setFinalDecisionType(dispute.getFinalDecisionType());
        vo.setFinalExecutionStatus(dispute.getFinalExecutionStatus());
        vo.setFinalResultDescription(dispute.getFinalResultDescription());
        vo.setNextActionHint(buildNextActionHint(dispute));
        return vo;
    }

    @Override
    @Transactional
    public Boolean sellerRespond(SellerDisputeResponseDTO dto, Long sellerId) {
        checkAndMarkTimeout();
        DisputeRequestEntity dispute = getById(dto.getDisputeId());
        if (dispute == null) {
            throw new BusinessException("争议不存在");
        }
        if (!Objects.equals(dispute.getSellerId(), sellerId)) {
            throw new BusinessException("无权限响应该争议");
        }
        if (!DisputeStatusEnum.canSellerRespond(dispute.getStatus())) {
            throw new BusinessException("当前争议状态不允许卖家响应");
        }

        String responseType = normalizeUpper(dto.getResponseType());
        if (!SellerResponseTypeEnum.contains(responseType)) {
            throw new BusinessException("responseType仅支持AGREE/REJECT/PROPOSE");
        }

        Integer currentRound = dispute.getCurrentRound() == null ? 1 : dispute.getCurrentRound();
        boolean isLateResponse = DisputeStatusEnum.SELLER_TIMEOUT.getCode().equals(dispute.getStatus());
        dispute.setCurrentRound(currentRound + 1);
        dispute.setSellerResponseType(responseType);
        dispute.setSellerResponseDescription(trimToEmpty(dto.getResponseDescription()));
        dispute.setSellerResponseAmount(dto.getProposalAmount());
        dispute.setSellerResponseFreightBearer(trimToEmpty(dto.getFreightBearer()));
        dispute.setExpireTime(null);

        if (SellerResponseTypeEnum.AGREE.getCode().equals(responseType)) {
            dispute.setSellerResponseProposalType(dispute.getRequestType());
            dispute.setSellerResponseAmount(dispute.getExpectedAmount());
            dispute.setStatus(DisputeStatusEnum.NEGOTIATION_SUCCESS.getCode());
            if (StringUtils.hasText(dispute.getSellerResponseDescription())) {
                recordLog(dispute.getId(), dispute.getCurrentRound(), sellerId, DisputeActorRoleEnum.SELLER.getCode(),
                        DisputeNegotiationActionEnum.SELLER_AGREE.getCode(),
                        dispute.getSellerResponseDescription(), dispute.getSellerResponseAmount());
            } else {
                recordLog(dispute.getId(), dispute.getCurrentRound(), sellerId, DisputeActorRoleEnum.SELLER.getCode(),
                        DisputeNegotiationActionEnum.SELLER_AGREE.getCode(),
                        "卖家同意买家诉求", dispute.getSellerResponseAmount());
            }
            executeNegotiationResult(dispute, dispute.getRequestType(), dispute.getExpectedAmount(), dispute.getRequestDescription());
        } else if (SellerResponseTypeEnum.REJECT.getCode().equals(responseType)) {
            dispute.setStatus(DisputeStatusEnum.NEGOTIATION_FAILED.getCode());
            recordLog(dispute.getId(), dispute.getCurrentRound(), sellerId, DisputeActorRoleEnum.SELLER.getCode(),
                    DisputeNegotiationActionEnum.SELLER_REJECT.getCode(),
                    firstText(dto.getResponseDescription(), "卖家拒绝买家诉求"), null);
        } else {
            if (!StringUtils.hasText(dto.getProposalType())) {
                throw new BusinessException("proposalType不能为空");
            }
            if (!StringUtils.hasText(dto.getProposalDescription())) {
                throw new BusinessException("proposalDescription不能为空");
            }
            dispute.setSellerResponseProposalType(normalizeUpper(dto.getProposalType()));
            dispute.setSellerResponseDescription(dto.getProposalDescription().trim());
            dispute.setStatus(DisputeStatusEnum.WAIT_BUYER_CONFIRM.getCode());
            dispute.setExpireTime(LocalDateTime.now().plusHours(BUYER_CONFIRM_TIMEOUT_HOURS));
            recordLog(dispute.getId(), dispute.getCurrentRound(), sellerId, DisputeActorRoleEnum.SELLER.getCode(),
                    DisputeNegotiationActionEnum.SELLER_PROPOSE.getCode(), buildProposalText(dispute), dispute.getSellerResponseAmount());
        }

        if (!updateById(dispute)) {
            throw new BusinessException("卖家响应失败");
        }
        if (isLateResponse) {
            recordLog(dispute.getId(), dispute.getCurrentRound(), sellerId, DisputeActorRoleEnum.SELLER.getCode(),
                    DisputeNegotiationActionEnum.SELLER_LATE_RESPONSE.getCode(), "卖家在超时后补响应", null);
        }
        return true;
    }

    @Override
    @Transactional
    public Boolean buyerConfirmProposal(BuyerConfirmProposalDTO dto, Long buyerId) {
        checkAndMarkTimeout();
        DisputeRequestEntity dispute = getById(dto.getDisputeId());
        if (dispute == null) {
            throw new BusinessException("争议不存在");
        }
        if (!Objects.equals(dispute.getBuyerId(), buyerId)) {
            throw new BusinessException("无权限确认该争议方案");
        }
        if (!DisputeStatusEnum.WAIT_BUYER_CONFIRM.getCode().equals(dispute.getStatus())) {
            throw new BusinessException("当前状态不允许买家确认方案");
        }

        if (Boolean.TRUE.equals(dto.getAcceptProposal())) {
            dispute.setStatus(DisputeStatusEnum.NEGOTIATION_SUCCESS.getCode());
            dispute.setExpireTime(null);
            if (!StringUtils.hasText(dispute.getSellerResponseProposalType())) {
                dispute.setSellerResponseProposalType(dispute.getRequestType());
            }
            if (!updateById(dispute)) {
                throw new BusinessException("买家确认失败");
            }
            recordLog(dispute.getId(), dispute.getCurrentRound(), buyerId, DisputeActorRoleEnum.BUYER.getCode(),
                    DisputeNegotiationActionEnum.BUYER_ACCEPT_PROPOSAL.getCode(),
                    firstText(dto.getRemark(), "买家接受卖家方案"), dispute.getSellerResponseAmount());
            executeNegotiationResult(dispute, dispute.getSellerResponseProposalType(),
                    dispute.getSellerResponseAmount(), dispute.getSellerResponseDescription());
            return true;
        }

        dispute.setStatus(DisputeStatusEnum.NEGOTIATION_FAILED.getCode());
        dispute.setExpireTime(null);
        if (!updateById(dispute)) {
            throw new BusinessException("买家拒绝方案失败");
        }
        recordLog(dispute.getId(), dispute.getCurrentRound(), buyerId, DisputeActorRoleEnum.BUYER.getCode(),
                DisputeNegotiationActionEnum.BUYER_REJECT_PROPOSAL.getCode(),
                firstText(dto.getRemark(), "买家拒绝卖家方案并升级仲裁"), null);
        doEscalateToArbitration(dispute, buyerId, firstText(dto.getRemark(), "买家拒绝卖家方案"), true);
        return true;
    }

    @Override
    @Transactional
    public Long escalateToArbitration(DisputeEscalateDTO dto, Long buyerId) {
        checkAndMarkTimeout();
        DisputeRequestEntity dispute = getById(dto.getDisputeId());
        if (dispute == null) {
            throw new BusinessException("争议不存在");
        }
        if (!Objects.equals(dispute.getBuyerId(), buyerId)) {
            throw new BusinessException("仅买家可升级仲裁");
        }
        if (dispute.getEscalatedArbitrationId() != null) {
            return dispute.getEscalatedArbitrationId();
        }
        if (!DisputeStatusEnum.canEscalate(dispute.getStatus())) {
            throw new BusinessException("当前状态不允许升级仲裁");
        }
        return doEscalateToArbitration(dispute, buyerId, dto.getEscalateReason(), false);
    }

    @Override
    @Transactional
    public Integer checkAndMarkTimeout() {
        QueryWrapper<DisputeRequestEntity> wrapper = new QueryWrapper<>();
        wrapper.in("status", Arrays.asList(
                        DisputeStatusEnum.WAIT_SELLER_RESPONSE.getCode(),
                        DisputeStatusEnum.WAIT_BUYER_CONFIRM.getCode()))
                .isNotNull("expire_time")
                .lt("expire_time", LocalDateTime.now());
        List<DisputeRequestEntity> timeoutDisputes = list(wrapper);
        if (timeoutDisputes.isEmpty()) {
            return 0;
        }
        int changed = 0;
        for (DisputeRequestEntity dispute : timeoutDisputes) {
            if (DisputeStatusEnum.WAIT_SELLER_RESPONSE.getCode().equals(dispute.getStatus())) {
                dispute.setStatus(DisputeStatusEnum.SELLER_TIMEOUT.getCode());
                if (updateById(dispute)) {
                    changed++;
                    recordLog(dispute.getId(), dispute.getCurrentRound(), 0L, DisputeActorRoleEnum.SYSTEM.getCode(),
                            DisputeNegotiationActionEnum.SELLER_TIMEOUT.getCode(), "卖家在24小时内未响应", null);
                }
            } else if (DisputeStatusEnum.WAIT_BUYER_CONFIRM.getCode().equals(dispute.getStatus())) {
                dispute.setStatus(DisputeStatusEnum.NEGOTIATION_FAILED.getCode());
                if (updateById(dispute)) {
                    changed++;
                    recordLog(dispute.getId(), dispute.getCurrentRound(), 0L, DisputeActorRoleEnum.SYSTEM.getCode(),
                            DisputeNegotiationActionEnum.BUYER_CONFIRM_TIMEOUT.getCode(),
                            "买家在24小时内未确认卖家方案，协商判定失败，可升级仲裁", null);
                }
            }
        }
        return changed;
    }

    @Override
    public String buildNegotiationSummary(Long disputeId) {
        DisputeRequestEntity dispute = getById(disputeId);
        if (dispute == null) {
            return "争议不存在";
        }
        List<DisputeNegotiationLogEntity> logs = listNegotiationLog(disputeId);
        logs.sort(Comparator.comparing(DisputeNegotiationLogEntity::getCreateTime, Comparator.nullsLast(Comparator.naturalOrder())));
        StringBuilder builder = new StringBuilder();
        builder.append("争议ID=").append(disputeId)
                .append("，当前状态=").append(DisputeStatusEnum.getLabel(dispute.getStatus()))
                .append("，轮次=").append(dispute.getCurrentRound() == null ? 1 : dispute.getCurrentRound());
        if (DisputeStatusEnum.NEGOTIATION_SUCCESS.getCode().equals(dispute.getStatus())) {
            NegotiationExecution execution = resolveNegotiationExecution(dispute);
            builder.append("，执行语义=").append(execution.getLabel());
            builder.append("，执行说明=").append(execution.getRemark());
        }
        if (!logs.isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            int count = 0;
            for (DisputeNegotiationLogEntity logEntity : logs) {
                if (count >= 12) {
                    builder.append("；...（其余记录省略）");
                    break;
                }
                builder.append("；[").append(logEntity.getCreateTime() == null ? "-" : formatter.format(logEntity.getCreateTime()))
                        .append("] ").append(DisputeNegotiationActionEnum.labelOf(logEntity.getActionType()))
                        .append(" - ").append(firstText(logEntity.getContent(), "-"));
                count++;
            }
        }
        return builder.toString();
    }

    @Override
    public List<DisputeChatSummaryVO> buildChatSummaryByOrder(Long orderId, Long buyerId, Long sellerId,
                                                              LocalDateTime startTime, LocalDateTime endTime) {
        if (orderId == null || buyerId == null || sellerId == null) {
            return buildEmptyChatSummary("聊天摘要参数不完整");
        }

        OrderDTO order = fetchOrderQuietly(orderId);
        List<ChatWindow> windows = buildChatWindows(order, startTime, endTime);
        List<MessageChatRecordDTO> messages = fetchConversationMessages(buyerId, sellerId);
        if (messages.isEmpty()) {
            return buildEmptyChatSummary("未检索到聊天消息");
        }

        Long orderProductId = order == null ? null : order.getProductId();
        List<MessageChatRecordDTO> relevantMessages = messages.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getCreateTime() != null)
                .filter(item -> inAnyWindow(item.getCreateTime(), windows))
                .filter(item -> matchOrder(orderProductId, item.getProductId()))
                .filter(item -> containsDisputeKeyword(item.getContent()))
                .sorted(Comparator.comparing(MessageChatRecordDTO::getCreateTime))
                .collect(Collectors.toList());

        if (relevantMessages.isEmpty()) {
            return buildEmptyChatSummary("时间窗内未命中争议关键词消息");
        }

        List<DisputeChatSummaryVO> result = new ArrayList<>();
        for (int i = 0; i < relevantMessages.size() && i < CHAT_RESULT_LIMIT; i++) {
            MessageChatRecordDTO message = relevantMessages.get(i);
            DisputeChatSummaryVO item = new DisputeChatSummaryVO();
            item.setSpeaker(resolveSpeakerByUserId(message.getSenderId(), buyerId, sellerId));
            item.setRole(resolveRoleByUserId(message.getSenderId(), buyerId, sellerId));
            item.setTime(message.getCreateTime());
            item.setSourceType("MESSAGE_RULE");
            item.setContent(buildSummaryContent(message.getContent(), locateWindowLabel(message.getCreateTime(), windows)));
            result.add(item);
        }
        return result;
    }

    private void refreshSingleTimeout(Long disputeId) {
        DisputeRequestEntity dispute = getById(disputeId);
        if (dispute == null || dispute.getExpireTime() == null) {
            return;
        }
        if (LocalDateTime.now().isBefore(dispute.getExpireTime())) {
            return;
        }
        if (DisputeStatusEnum.WAIT_SELLER_RESPONSE.getCode().equals(dispute.getStatus())
                || DisputeStatusEnum.WAIT_BUYER_CONFIRM.getCode().equals(dispute.getStatus())) {
            checkAndMarkTimeout();
        }
    }

    private IPage<DisputeListItemVO> toListItemPage(IPage<DisputeRequestEntity> entityPage) {
        Page<DisputeListItemVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<DisputeListItemVO> records = entityPage.getRecords().stream().map(this::toListItemVO).collect(Collectors.toList());
        voPage.setRecords(records);
        return voPage;
    }

    private DisputeListItemVO toListItemVO(DisputeRequestEntity entity) {
        DisputeListItemVO vo = new DisputeListItemVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setStatusLabel(DisputeStatusEnum.getLabel(entity.getStatus()));
        vo.setCanEscalate(DisputeStatusEnum.canEscalate(entity.getStatus()));
        return vo;
    }

    private DisputeEvidenceVO toEvidenceVO(DisputeEvidenceEntity entity) {
        DisputeEvidenceVO vo = new DisputeEvidenceVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    private DisputeNegotiationLogVO toLogVO(DisputeNegotiationLogEntity entity) {
        DisputeNegotiationLogVO vo = new DisputeNegotiationLogVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setActionLabel(DisputeNegotiationActionEnum.labelOf(entity.getActionType()));
        return vo;
    }

    private SellerProposalVO buildSellerProposal(DisputeRequestEntity dispute) {
        if (!StringUtils.hasText(dispute.getSellerResponseType())) {
            return null;
        }
        SellerProposalVO vo = new SellerProposalVO();
        vo.setProposalType(firstText(dispute.getSellerResponseProposalType(), dispute.getRequestType()));
        vo.setProposalAmount(dispute.getSellerResponseAmount());
        vo.setProposalDescription(dispute.getSellerResponseDescription());
        vo.setFreightBearer(dispute.getSellerResponseFreightBearer());
        return vo;
    }

    private void ensureNoActiveDispute(Long orderId, Long buyerId) {
        QueryWrapper<DisputeRequestEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId)
                .eq("buyer_id", buyerId)
                .notIn("status", Arrays.asList(
                        DisputeStatusEnum.NEGOTIATION_SUCCESS.getCode(),
                        DisputeStatusEnum.CLOSED.getCode(),
                        DisputeStatusEnum.ARBITRATION_EXECUTED.getCode()));
        if (count(wrapper) > 0) {
            throw new BusinessException("该订单已有进行中的争议，请先处理当前争议");
        }
    }

    private OrderDTO fetchAndValidateOrder(Long orderId, Long buyerId) {
        Result<OrderDTO> result;
        try {
            result = tradeFeignClient.getOrderById(orderId);
        } catch (Exception e) {
            throw new BusinessException("查询订单失败，请稍后重试");
        }
        if (result == null || !result.isSuccess() || result.getData() == null) {
            throw new BusinessException("订单不存在或不可用");
        }
        OrderDTO orderDTO = result.getData();
        if (!Objects.equals(orderDTO.getBuyerId(), buyerId)) {
            throw new BusinessException("仅买家可发起争议");
        }
        if (!Arrays.asList(1, 2, 5).contains(orderDTO.getStatus())) {
            throw new BusinessException("当前订单状态不允许发起争议");
        }
        return orderDTO;
    }

    private void saveDisputeEvidence(Long disputeId, List<DisputeCreateDTO.EvidenceItemDTO> evidenceList,
                                     Long uploaderId, String uploaderRole) {
        if (evidenceList == null || evidenceList.isEmpty()) {
            return;
        }
        for (DisputeCreateDTO.EvidenceItemDTO item : evidenceList) {
            if (item == null || !StringUtils.hasText(item.getFileUrl())) {
                continue;
            }
            DisputeEvidenceEntity evidenceEntity = new DisputeEvidenceEntity();
            evidenceEntity.setBizType(EVIDENCE_BIZ_DISPUTE);
            evidenceEntity.setBizId(disputeId);
            evidenceEntity.setUploaderId(uploaderId);
            evidenceEntity.setUploaderRole(uploaderRole);
            evidenceEntity.setEvidenceType(firstText(item.getEvidenceType(), "IMAGE"));
            evidenceEntity.setTitle(trimToEmpty(item.getTitle()));
            evidenceEntity.setDescription(trimToEmpty(item.getDescription()));
            evidenceEntity.setFileUrl(item.getFileUrl().trim());
            evidenceEntity.setThumbnailUrl(trimToEmpty(item.getThumbnailUrl()));
            disputeEvidenceMapper.insert(evidenceEntity);
        }
    }

    private List<DisputeEvidenceEntity> listDisputeEvidence(Long disputeId) {
        QueryWrapper<DisputeEvidenceEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("biz_type", EVIDENCE_BIZ_DISPUTE)
                .eq("biz_id", disputeId)
                .orderByAsc("create_time");
        return disputeEvidenceMapper.selectList(wrapper);
    }

    private List<DisputeEvidenceEntity> listDisputeEvidenceByRole(Long disputeId, String role) {
        QueryWrapper<DisputeEvidenceEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("biz_type", EVIDENCE_BIZ_DISPUTE)
                .eq("biz_id", disputeId)
                .eq("uploader_role", role)
                .orderByAsc("create_time");
        return disputeEvidenceMapper.selectList(wrapper);
    }

    private List<DisputeNegotiationLogEntity> listNegotiationLog(Long disputeId) {
        QueryWrapper<DisputeNegotiationLogEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("dispute_id", disputeId).orderByAsc("create_time");
        return disputeNegotiationLogMapper.selectList(wrapper);
    }

    private void executeNegotiationResult(DisputeRequestEntity dispute, String proposalType,
                                          BigDecimal proposalAmount, String description) {
        NegotiationExecution execution = resolveExecutionByProposalType(proposalType, proposalAmount);
        String executeRemark = "协商已达成，执行语义：" + execution.getLabel()
                + "。当前仅完成协商状态收口，等待后续执行，尚未触发真实退款/退货链路。";
        if (StringUtils.hasText(description)) {
            executeRemark = executeRemark + "；方案说明：" + description.trim();
        }
        recordLog(dispute.getId(), dispute.getCurrentRound(), 0L, DisputeActorRoleEnum.SYSTEM.getCode(),
                DisputeNegotiationActionEnum.SYSTEM_AUTO_EXECUTE.getCode(), executeRemark, proposalAmount);
    }

    private Long doEscalateToArbitration(DisputeRequestEntity dispute, Long operatorId, String escalateReason, boolean fromRejectFlow) {
        if (dispute.getEscalatedArbitrationId() != null) {
            return dispute.getEscalatedArbitrationId();
        }
        List<DisputeEvidenceEntity> buyerEvidence = listDisputeEvidenceByRole(dispute.getId(), DisputeActorRoleEnum.BUYER.getCode());
        List<String> buyerEvidenceUrls = buyerEvidence.stream()
                .map(DisputeEvidenceEntity::getFileUrl)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());

        ArbitrationEntity arbitration = new ArbitrationEntity();
        arbitration.setOrderId(dispute.getOrderId());
        arbitration.setApplicantId(dispute.getBuyerId());
        arbitration.setRespondentId(dispute.getSellerId());
        arbitration.setReason(dispute.getReason());
        arbitration.setDescription(dispute.getFactDescription());
        arbitration.setEvidence(toJson(buyerEvidenceUrls));
        arbitration.setStatus(ARBITRATION_PENDING);
        arbitration.setSourceDisputeId(dispute.getId());
        arbitration.setRequestType(dispute.getRequestType());
        arbitration.setRequestDescription(dispute.getRequestDescription());
        arbitration.setExpectedAmount(dispute.getExpectedAmount());
        arbitration.setBuyerClaim(dispute.getFactDescription());
        arbitration.setCreateTime(LocalDateTime.now());
        arbitration.setUpdateTime(LocalDateTime.now());
        arbitrationMapper.insert(arbitration);

        ArbitrationLogEntity arbitrationLog = new ArbitrationLogEntity();
        arbitrationLog.setArbitrationId(arbitration.getId());
        arbitrationLog.setOperatorId(operatorId);
        arbitrationLog.setAction("SUBMIT");
        arbitrationLog.setRemark("争议升级仲裁，sourceDisputeId=" + dispute.getId());
        arbitrationLogService.save(arbitrationLog);

        ArbitrationEvidenceSubmissionEntity buyerSubmission = new ArbitrationEvidenceSubmissionEntity();
        buyerSubmission.setArbitrationId(arbitration.getId());
        buyerSubmission.setSubmitterId(dispute.getBuyerId());
        buyerSubmission.setSubmitterRole(DisputeActorRoleEnum.BUYER.getCode());
        buyerSubmission.setClaimText(null);
        buyerSubmission.setFactText(dispute.getFactDescription());
        buyerSubmission.setEvidenceUrls(toJson(buyerEvidenceUrls));
        buyerSubmission.setNote("由争议协商阶段自动继承");
        buyerSubmission.setIsLate(0);
        arbitrationEvidenceSubmissionService.save(buyerSubmission);

        if (StringUtils.hasText(dispute.getSellerResponseDescription())) {
            ArbitrationEvidenceSubmissionEntity sellerSubmission = new ArbitrationEvidenceSubmissionEntity();
            sellerSubmission.setArbitrationId(arbitration.getId());
            sellerSubmission.setSubmitterId(dispute.getSellerId());
            sellerSubmission.setSubmitterRole(DisputeActorRoleEnum.SELLER.getCode());
            sellerSubmission.setClaimText(dispute.getSellerResponseDescription());
            sellerSubmission.setFactText(null);
            sellerSubmission.setEvidenceUrls("[]");
            sellerSubmission.setNote("卖家协商阶段响应自动继承");
            sellerSubmission.setIsLate(0);
            arbitrationEvidenceSubmissionService.save(sellerSubmission);
        }

        ArbitrationEvidenceSubmissionEntity systemSubmission = new ArbitrationEvidenceSubmissionEntity();
        systemSubmission.setArbitrationId(arbitration.getId());
        systemSubmission.setSubmitterId(0L);
        systemSubmission.setSubmitterRole(DisputeActorRoleEnum.SYSTEM.getCode());
        systemSubmission.setClaimText(buildNegotiationSummary(dispute.getId()));
        systemSubmission.setFactText(firstText(escalateReason, fromRejectFlow ? "买家拒绝卖家方案后升级仲裁" : "争议协商失败，升级仲裁"));
        systemSubmission.setEvidenceUrls("[]");
        systemSubmission.setNote("协商摘要");
        systemSubmission.setIsLate(0);
        arbitrationEvidenceSubmissionService.save(systemSubmission);

        QueryWrapper<DisputeEvidenceEntity> evidenceWrapper = new QueryWrapper<>();
        evidenceWrapper.eq("biz_type", EVIDENCE_BIZ_DISPUTE).eq("biz_id", dispute.getId());
        List<DisputeEvidenceEntity> allEvidence = disputeEvidenceMapper.selectList(evidenceWrapper);
        for (DisputeEvidenceEntity evidenceEntity : allEvidence) {
            DisputeEvidenceEntity copy = new DisputeEvidenceEntity();
            BeanUtils.copyProperties(evidenceEntity, copy, "id", "createTime");
            copy.setBizType(EVIDENCE_BIZ_ARBITRATION);
            copy.setBizId(arbitration.getId());
            disputeEvidenceMapper.insert(copy);
        }

        dispute.setEscalatedArbitrationId(arbitration.getId());
        dispute.setStatus(DisputeStatusEnum.ESCALATED_TO_ARBITRATION.getCode());
        dispute.setFinalDecisionType(null);
        dispute.setFinalExecutionStatus(null);
        dispute.setFinalResultDescription("争议已升级仲裁，等待平台裁决");
        dispute.setExpireTime(null);
        updateById(dispute);

        recordLog(dispute.getId(), dispute.getCurrentRound(), operatorId, DisputeActorRoleEnum.BUYER.getCode(),
                DisputeNegotiationActionEnum.ESCALATE_TO_ARBITRATION.getCode(),
                firstText(escalateReason, "协商失败，升级仲裁"), null);
        return arbitration.getId();
    }

    private void recordLog(Long disputeId, Integer roundNo, Long actorId, String actorRole,
                           String actionType, String content, BigDecimal amount) {
        DisputeNegotiationLogEntity logEntity = new DisputeNegotiationLogEntity();
        logEntity.setDisputeId(disputeId);
        logEntity.setRoundNo(roundNo == null ? 1 : roundNo);
        logEntity.setActorId(actorId);
        logEntity.setActorRole(actorRole);
        logEntity.setActionType(actionType);
        logEntity.setContent(content);
        logEntity.setAmount(amount);
        disputeNegotiationLogMapper.insert(logEntity);
    }

    private boolean hitHighRiskRule(DisputeRequestEntity dispute) {
        return dispute.getExpectedAmount() != null
                && dispute.getExpectedAmount().compareTo(HIGH_AMOUNT_ESCALATE_THRESHOLD) >= 0;
    }

    private Long calcCountdownSeconds(LocalDateTime expireTime) {
        if (expireTime == null) {
            return 0L;
        }
        long seconds = Duration.between(LocalDateTime.now(), expireTime).getSeconds();
        return Math.max(seconds, 0L);
    }

    private String buildProposalText(DisputeRequestEntity dispute) {
        StringBuilder builder = new StringBuilder();
        builder.append("proposalType=").append(firstText(dispute.getSellerResponseProposalType(), "-"));
        if (dispute.getSellerResponseAmount() != null) {
            builder.append("，proposalAmount=").append(dispute.getSellerResponseAmount().toPlainString());
        }
        if (StringUtils.hasText(dispute.getSellerResponseDescription())) {
            builder.append("，proposalDescription=").append(dispute.getSellerResponseDescription().trim());
        }
        if (StringUtils.hasText(dispute.getSellerResponseFreightBearer())) {
            builder.append("，freightBearer=").append(dispute.getSellerResponseFreightBearer().trim());
        }
        return builder.toString();
    }

    private List<MessageChatRecordDTO> fetchConversationMessages(Long buyerId, Long sellerId) {
        List<MessageChatRecordDTO> allMessages = new ArrayList<>();
        for (int pageNum = 1; pageNum <= CHAT_MAX_PAGES; pageNum++) {
            Result<MessageChatHistoryPageDTO> response;
            try {
                response = messageFeignClient.getChatHistory(buyerId, sellerId, pageNum, CHAT_PAGE_SIZE);
            } catch (Exception e) {
                log.warn("fetch chat history failed, buyerId={}, sellerId={}, pageNum={}", buyerId, sellerId, pageNum, e);
                break;
            }

            if (response == null || !response.isSuccess() || response.getData() == null) {
                break;
            }
            MessageChatHistoryPageDTO pageData = response.getData();
            List<MessageChatRecordDTO> records = pageData.getRecords();
            if (records == null || records.isEmpty()) {
                break;
            }
            allMessages.addAll(records);

            Long pages = pageData.getPages();
            if ((pages != null && pages > 0 && pageNum >= pages) || records.size() < CHAT_PAGE_SIZE) {
                break;
            }
        }
        return allMessages;
    }

    private List<ChatWindow> buildChatWindows(OrderDTO order, LocalDateTime disputeStart, LocalDateTime endTime) {
        List<ChatWindow> windows = new ArrayList<>();
        if (order != null && order.getCreateTime() != null) {
            LocalDateTime orderCreateTime = order.getCreateTime();
            windows.add(new ChatWindow("下单前3天", orderCreateTime.minusDays(3), orderCreateTime));

            LocalDate orderDate = orderCreateTime.toLocalDate();
            windows.add(new ChatWindow("下单当天", orderDate.atStartOfDay(), orderDate.plusDays(1).atStartOfDay()));
        }
        if (disputeStart != null) {
            windows.add(new ChatWindow("争议发起前后24小时", disputeStart.minusHours(24), disputeStart.plusHours(24)));
        }
        if (windows.isEmpty()) {
            LocalDateTime fallbackStart = disputeStart == null ? LocalDateTime.now().minusDays(3) : disputeStart.minusDays(1);
            windows.add(new ChatWindow("默认时间窗", fallbackStart, endTime == null ? LocalDateTime.now() : endTime));
        }
        return windows;
    }

    private boolean inAnyWindow(LocalDateTime time, List<ChatWindow> windows) {
        for (ChatWindow window : windows) {
            if (window.contains(time)) {
                return true;
            }
        }
        return false;
    }

    private String locateWindowLabel(LocalDateTime time, List<ChatWindow> windows) {
        for (ChatWindow window : windows) {
            if (window.contains(time)) {
                return window.getLabel();
            }
        }
        return "时间窗外";
    }

    private boolean containsDisputeKeyword(String content) {
        if (!StringUtils.hasText(content)) {
            return false;
        }
        String normalized = content.trim().toLowerCase(Locale.ROOT);
        for (String keyword : DISPUTE_CHAT_KEYWORDS) {
            if (normalized.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private boolean matchOrder(Long orderProductId, Long messageProductId) {
        if (orderProductId == null || messageProductId == null) {
            return true;
        }
        return Objects.equals(orderProductId, messageProductId);
    }

    private String buildSummaryContent(String rawContent, String windowLabel) {
        String content = trimToEmpty(rawContent);
        if (content.length() > 160) {
            content = content.substring(0, 160) + "...";
        }
        return "[" + windowLabel + "] " + content;
    }

    private String resolveSpeakerByUserId(Long senderId, Long buyerId, Long sellerId) {
        if (Objects.equals(senderId, buyerId)) {
            return "买家";
        }
        if (Objects.equals(senderId, sellerId)) {
            return "卖家";
        }
        return "系统";
    }

    private String resolveRoleByUserId(Long senderId, Long buyerId, Long sellerId) {
        if (Objects.equals(senderId, buyerId)) {
            return DisputeActorRoleEnum.BUYER.getCode();
        }
        if (Objects.equals(senderId, sellerId)) {
            return DisputeActorRoleEnum.SELLER.getCode();
        }
        return DisputeActorRoleEnum.SYSTEM.getCode();
    }

    private List<DisputeChatSummaryVO> buildEmptyChatSummary(String message) {
        DisputeChatSummaryVO item = new DisputeChatSummaryVO();
        item.setSpeaker("系统");
        item.setRole(DisputeActorRoleEnum.SYSTEM.getCode());
        item.setTime(LocalDateTime.now());
        item.setSourceType("EMPTY");
        item.setContent(firstText(message, "暂无可用聊天摘要"));
        return new ArrayList<>(Collections.singletonList(item));
    }

    private OrderDTO fetchOrderQuietly(Long orderId) {
        if (orderId == null) {
            return null;
        }
        try {
            Result<OrderDTO> result = tradeFeignClient.getOrderById(orderId);
            if (result != null && result.isSuccess()) {
                return result.getData();
            }
        } catch (Exception e) {
            log.warn("fetch order failed, orderId={}", orderId, e);
        }
        return null;
    }

    private NegotiationExecution resolveNegotiationExecution(DisputeRequestEntity dispute) {
        if (dispute == null || !DisputeStatusEnum.NEGOTIATION_SUCCESS.getCode().equals(dispute.getStatus())) {
            return new NegotiationExecution(
                    EXECUTION_STATUS_NOT_APPLICABLE,
                    "当前不处于协商成功状态",
                    "尚未进入协商执行阶段"
            );
        }
        String proposalType;
        if (SellerResponseTypeEnum.AGREE.getCode().equals(dispute.getSellerResponseType())) {
            proposalType = dispute.getRequestType();
        } else {
            proposalType = firstText(dispute.getSellerResponseProposalType(), dispute.getRequestType());
        }
        BigDecimal amount = dispute.getSellerResponseAmount() == null ? dispute.getExpectedAmount() : dispute.getSellerResponseAmount();
        return resolveExecutionByProposalType(proposalType, amount);
    }

    private NegotiationExecution resolveExecutionByProposalType(String proposalType, BigDecimal amount) {
        String normalized = normalizeUpper(proposalType);
        if ("FULL_REFUND".equals(normalized)) {
            return new NegotiationExecution(
                    EXECUTION_STATUS_FULL_REFUND_ACCEPTED,
                    "全额退款已协商达成（待执行）",
                    "协商已达成全额退款，等待后续执行链路"
            );
        }
        if ("PARTIAL_REFUND".equals(normalized)) {
            String amountText = amount == null ? "0.00" : amount.toPlainString();
            return new NegotiationExecution(
                    EXECUTION_STATUS_PARTIAL_REFUND_ACCEPTED,
                    "部分退款已协商达成（待执行）",
                    "协商已达成部分退款，金额 " + amountText + "，等待后续执行链路"
            );
        }
        if ("RETURN_AND_REFUND".equals(normalized)) {
            return new NegotiationExecution(
                    EXECUTION_STATUS_RETURN_AND_REFUND_PENDING,
                    "退货退款已协商达成（待执行）",
                    "协商已达成退货退款，等待后续履约与退款执行"
            );
        }
        return new NegotiationExecution(
                EXECUTION_STATUS_NEGOTIATION_SUCCESS,
                "协商成功（待后续执行）",
                "协商已达成，等待后续执行链路"
        );
    }

    private String buildNextActionHint(DisputeRequestEntity dispute) {
        if (dispute == null) {
            return "";
        }
        String status = dispute.getStatus();
        String decisionType = normalizeUpper(dispute.getFinalDecisionType());
        if (DisputeStatusEnum.ARBITRATION_DECIDED.getCode().equals(status)
                || DisputeStatusEnum.ARBITRATION_EXECUTING.getCode().equals(status)) {
            if ("SUPPORT_FULL_REFUND".equals(decisionType)) {
                return "平台已裁决支持全额退款，等待系统执行";
            }
            if ("SUPPORT_PARTIAL_REFUND".equals(decisionType)) {
                return "平台已裁决支持部分退款，等待系统执行";
            }
            if ("SUPPORT_RETURN_AND_REFUND".equals(decisionType)) {
                return "平台已裁决支持退货退款，请买家先寄回商品";
            }
            if ("SUPPORT_REPLACE".equals(decisionType)) {
                return "平台已裁决支持换货/补发，请双方按指引履约";
            }
            if ("REJECT_BUYER_REQUEST".equals(decisionType)) {
                return "平台已驳回本次争议申请";
            }
            return firstText(dispute.getFinalResultDescription(), "平台已裁决，等待后续执行");
        }
        if (DisputeStatusEnum.ARBITRATION_EXECUTED.getCode().equals(status)) {
            return firstText(dispute.getFinalResultDescription(), "仲裁执行已完成");
        }
        if (DisputeStatusEnum.CLOSED.getCode().equals(status) && "REJECT_BUYER_REQUEST".equals(decisionType)) {
            return "平台已驳回本次争议申请";
        }
        return firstText(dispute.getFinalResultDescription(), "");
    }

    private String toJson(List<String> urls) {
        List<String> safe = urls == null ? Collections.emptyList() : urls.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toList());
        try {
            return objectMapper.writeValueAsString(safe);
        } catch (Exception e) {
            log.warn("serialize urls failed", e);
            return "[]";
        }
    }

    private String normalizeUpper(String value) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase(Locale.ROOT) : "";
    }

    private String trimToEmpty(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }

    private String firstText(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private static class ChatWindow {
        private final String label;
        private final LocalDateTime start;
        private final LocalDateTime end;

        private ChatWindow(String label, LocalDateTime start, LocalDateTime end) {
            this.label = label;
            this.start = start;
            this.end = end;
        }

        private String getLabel() {
            return label;
        }

        private boolean contains(LocalDateTime time) {
            boolean afterStart = start == null || !time.isBefore(start);
            boolean beforeEnd = end == null || !time.isAfter(end);
            return afterStart && beforeEnd;
        }
    }

    private static class NegotiationExecution {
        private final String code;
        private final String label;
        private final String remark;

        private NegotiationExecution(String code, String label, String remark) {
            this.code = code;
            this.label = label;
            this.remark = remark;
        }

        private String getCode() {
            return code;
        }

        private String getLabel() {
            return label;
        }

        private String getRemark() {
            return remark;
        }
    }
}
