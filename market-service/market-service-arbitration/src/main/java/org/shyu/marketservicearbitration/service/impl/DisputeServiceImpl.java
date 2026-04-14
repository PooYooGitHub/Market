package org.shyu.marketservicearbitration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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
    private static final BigDecimal HIGH_AMOUNT_ESCALATE_THRESHOLD = new BigDecimal("5000.00");
    private static final String EVIDENCE_BIZ_DISPUTE = "DISPUTE";
    private static final String EVIDENCE_BIZ_ARBITRATION = "ARBITRATION";

    @Autowired
    private TradeFeignClient tradeFeignClient;

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
        List<DisputeChatSummaryVO> result = new ArrayList<>();
        DisputeChatSummaryVO context = new DisputeChatSummaryVO();
        context.setSpeaker("系统");
        context.setRole(DisputeActorRoleEnum.SYSTEM.getCode());
        context.setTime(LocalDateTime.now());
        context.setSourceType("RULE_CONTEXT");
        context.setContent("聊天摘要时间窗：" + firstText(String.valueOf(startTime), "-") + " ~ "
                + firstText(String.valueOf(endTime), "-")
                + "；当前版本基于协商日志规则构建，消息服务接入预留在该接口");
        result.add(context);

        QueryWrapper<DisputeRequestEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId).eq("buyer_id", buyerId).eq("seller_id", sellerId);
        if (startTime != null) {
            wrapper.ge("create_time", startTime);
        }
        if (endTime != null) {
            wrapper.le("create_time", endTime);
        }
        List<DisputeRequestEntity> disputes = list(wrapper);
        if (disputes.isEmpty()) {
            return result;
        }

        List<Long> disputeIds = disputes.stream().map(DisputeRequestEntity::getId).collect(Collectors.toList());
        QueryWrapper<DisputeNegotiationLogEntity> logWrapper = new QueryWrapper<>();
        logWrapper.in("dispute_id", disputeIds).orderByAsc("create_time");
        List<DisputeNegotiationLogEntity> logs = disputeNegotiationLogMapper.selectList(logWrapper);
        for (DisputeNegotiationLogEntity logEntity : logs) {
            DisputeChatSummaryVO item = new DisputeChatSummaryVO();
            item.setSpeaker(resolveSpeaker(logEntity.getActorRole()));
            item.setRole(logEntity.getActorRole());
            item.setTime(logEntity.getCreateTime());
            item.setSourceType("NEGOTIATION_LOG");
            item.setContent(firstText(logEntity.getContent(), DisputeNegotiationActionEnum.labelOf(logEntity.getActionType())));
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
                        DisputeStatusEnum.ESCALATED_TO_ARBITRATION.getCode()));
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
        String normalizedProposalType = normalizeUpper(firstText(proposalType, dispute.getRequestType()));
        String executeRemark;
        if ("FULL_REFUND".equals(normalizedProposalType)) {
            executeRemark = "系统进入全额退款流程";
        } else if ("PARTIAL_REFUND".equals(normalizedProposalType)) {
            executeRemark = "系统进入部分退款流程，金额：" + (proposalAmount == null ? "0" : proposalAmount.toPlainString());
        } else if ("RETURN_AND_REFUND".equals(normalizedProposalType)) {
            executeRemark = "系统进入退货退款流程（先退货后退款）";
        } else if ("REPLACE".equals(normalizedProposalType) || "RESEND".equals(normalizedProposalType)) {
            executeRemark = "系统预留补发/换货执行能力";
        } else {
            executeRemark = "系统进入协商成功自动处理流程";
        }
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
        arbitration.setDescription(dispute.getRequestDescription());
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

    private String resolveSpeaker(String role) {
        String normalized = normalizeUpper(role);
        if (DisputeActorRoleEnum.BUYER.getCode().equals(normalized)) {
            return "买家";
        }
        if (DisputeActorRoleEnum.SELLER.getCode().equals(normalized)) {
            return "卖家";
        }
        if (DisputeActorRoleEnum.ADMIN.getCode().equals(normalized)) {
            return "管理员";
        }
        return "系统";
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

    private List<String> parseJsonArray(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.singletonList(json);
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
}
