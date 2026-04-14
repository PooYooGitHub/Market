package org.shyu.marketservicearbitration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.shyu.marketapiarbitration.enums.ArbitrationReason;
import org.shyu.marketapiarbitration.enums.ArbitrationStatus;
import org.shyu.marketapiproduct.dto.ProductDTO;
import org.shyu.marketapiproduct.feign.ProductFeignClient;
import org.shyu.marketapitrade.dto.OrderDTO;
import org.shyu.marketapitrade.feign.TradeFeignClient;
import org.shyu.marketapiuser.dto.UserDTO;
import org.shyu.marketapiuser.feign.UserFeignClient;
import org.shyu.marketcommon.exception.BusinessException;
import org.shyu.marketcommon.result.Result;
import org.shyu.marketservicearbitration.dto.ArbitrationCompleteDTO;
import org.shyu.marketservicearbitration.dto.ArbitrationRejectDTO;
import org.shyu.marketservicearbitration.dto.SupplementRequestDTO;
import org.shyu.marketservicearbitration.dto.SupplementSubmitDTO;
import org.shyu.marketservicearbitration.entity.ArbitrationEntity;
import org.shyu.marketservicearbitration.entity.ArbitrationEvidenceSubmissionEntity;
import org.shyu.marketservicearbitration.entity.ArbitrationLogEntity;
import org.shyu.marketservicearbitration.entity.ArbitrationSupplementRequestEntity;
import org.shyu.marketservicearbitration.mapper.ArbitrationMapper;
import org.shyu.marketservicearbitration.service.IArbitrationEvidenceSubmissionService;
import org.shyu.marketservicearbitration.service.IArbitrationLogService;
import org.shyu.marketservicearbitration.service.IArbitrationService;
import org.shyu.marketservicearbitration.service.IArbitrationSupplementRequestService;
import org.shyu.marketservicearbitration.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ArbitrationServiceImpl extends ServiceImpl<ArbitrationMapper, ArbitrationEntity> implements IArbitrationService {

    private static final int PENDING = 0;
    private static final int PROCESSING = 1;
    private static final int COMPLETED = 2;
    private static final int REJECTED = 3;
    private static final int LEGACY_WAIT_SUPPLEMENT = 4;

    private static final int SR_PENDING = 0;
    private static final int SR_SUBMITTED = 1;
    private static final int SR_EXPIRED = 2;
    private static final int SR_CANCELED = 3;

    private static final String BUYER = "BUYER";
    private static final String SELLER = "SELLER";
    private static final String SYSTEM = "SYSTEM";

    @Autowired private IArbitrationLogService arbitrationLogService;
    @Autowired private IArbitrationSupplementRequestService supplementRequestService;
    @Autowired private IArbitrationEvidenceSubmissionService evidenceSubmissionService;
    @Autowired private TradeFeignClient tradeFeignClient;
    @Autowired private ProductFeignClient productFeignClient;
    @Autowired private UserFeignClient userFeignClient;

    private final ObjectMapper om = new ObjectMapper();

    @Override
    @Transactional
    public ArbitrationEntity submitArbitration(ArbitrationVO vo) {
        if (checkArbitrationExists(vo.getOrderId(), vo.getApplicantId())) throw new BusinessException("该订单已存在仲裁申请，请勿重复提交");
        ArbitrationEntity e = new ArbitrationEntity();
        BeanUtils.copyProperties(vo, e);
        e.setStatus(PENDING);
        e.setCreateTime(LocalDateTime.now());
        if (!save(e)) throw new BusinessException("仲裁申请提交失败");
        log(e.getId(), vo.getApplicantId(), "SUBMIT", "用户提交仲裁申请");
        try {
            ArbitrationEvidenceSubmissionEntity s = new ArbitrationEvidenceSubmissionEntity();
            s.setArbitrationId(e.getId());
            s.setSubmitterId(e.getApplicantId());
            s.setSubmitterRole(resolveRole(e, e.getApplicantId()));
            s.setClaimText(vo.getReason());
            s.setFactText(vo.getDescription());
            s.setEvidenceUrls(normalizeEvidenceJson(vo.getEvidence()));
            s.setNote("初始仲裁申请材料");
            s.setIsLate(0);
            evidenceSubmissionService.save(s);
        } catch (Exception ex) {
            log.warn("save initial evidence failed", ex);
        }
        return e;
    }

    @Override
    @Transactional
    public ArbitrationEntity updateArbitration(Long id, Long applicantId, ArbitrationVO vo) {
        ArbitrationEntity e = getById(id);
        if (e == null) throw new BusinessException("仲裁记录不存在");
        if (!Objects.equals(applicantId, e.getApplicantId())) throw new BusinessException("无权限修改该仲裁申请");
        if (Arrays.asList(COMPLETED, REJECTED).contains(e.getStatus())) throw new BusinessException("当前仲裁状态不可修改");
        e.setReason(vo.getReason());
        e.setDescription(vo.getDescription());
        e.setEvidence(vo.getEvidence());
        if (vo.getRespondentId() != null) e.setRespondentId(vo.getRespondentId());
        e.setUpdateTime(LocalDateTime.now());
        if (!updateById(e)) throw new BusinessException("修改仲裁申请失败");
        log(id, applicantId, "UPDATE", "用户修改仲裁申请");
        return e;
    }

    @Override
    public IPage<ArbitrationEntity> getArbitrationPage(Integer current, Integer size, Integer status, Long applicantId, Long respondentId) {
        return getArbitrationPage(current, size, status, applicantId, respondentId, null, null);
    }

    @Override
    public IPage<ArbitrationEntity> getArbitrationPage(Integer current, Integer size, Integer status, Long applicantId, Long respondentId, String keyword, String priority) {
        Page<ArbitrationEntity> p = new Page<>(current, size);
        QueryWrapper<ArbitrationEntity> q = new QueryWrapper<>();
        if (status != null) {
            if (Objects.equals(status, PROCESSING)) q.in("status", Arrays.asList(PROCESSING, LEGACY_WAIT_SUPPLEMENT));
            else q.eq("status", status);
        }
        if (applicantId != null) q.eq("applicant_id", applicantId);
        if (respondentId != null) q.eq("respondent_id", respondentId);
        if (StringUtils.hasText(keyword)) {
            String k = keyword.trim();
            q.and(w -> w.like("reason", k).or().like("description", k).or().like("result", k)
                    .or().like("order_id", k).or().like("applicant_id", k).or().like("respondent_id", k));
        }
        if (StringUtils.hasText(priority)) {
            String pty = priority.trim().toLowerCase(Locale.ROOT);
            LocalDateTime now = LocalDateTime.now();
            if ("high".equals(pty)) q.le("create_time", now.minusDays(3));
            else if ("normal".equals(pty)) q.between("create_time", now.minusDays(3), now.minusDays(1));
            else if ("low".equals(pty)) q.ge("create_time", now.minusDays(1));
        }
        q.orderByDesc("create_time");
        return page(p, q);
    }

    @Override
    public ArbitrationEntity getArbitrationDetail(Long id) {
        ArbitrationEntity e = getById(id);
        if (e == null) throw new BusinessException("仲裁记录不存在");
        return e;
    }

    @Override
    public IPage<AdminArbitrationListItemVO> getAdminArbitrationList(Integer current, Integer size,
                                                                      Integer status, String keyword, String priority) {
        IPage<ArbitrationEntity> page = getArbitrationPage(current, size, status, null, null, keyword, priority);
        Page<AdminArbitrationListItemVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<AdminArbitrationListItemVO> records = page.getRecords().stream()
                .map(this::toAdminListItem)
                .collect(Collectors.toList());
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    @Transactional
    public Boolean acceptAdminArbitration(Long id, Long handlerId) {
        ArbitrationEntity e = getById(id);
        if (e == null) throw new BusinessException("仲裁记录不存在");
        if (!Objects.equals(normalizeStatus(e.getStatus()), PENDING)) {
            throw new BusinessException("当前状态不允许受理");
        }
        e.setStatus(PROCESSING);
        e.setHandlerId(handlerId);
        e.setUpdateTime(LocalDateTime.now());
        if (!updateById(e)) throw new BusinessException("受理仲裁申请失败");
        log(id, handlerId, "ACCEPT", "平台受理案件");
        log(id, handlerId, "PROCESSING", "平台处理中");
        return true;
    }

    @Override
    @Transactional
    public Boolean completeAdminArbitration(ArbitrationCompleteDTO dto, Long handlerId) {
        ArbitrationEntity e = getById(dto.getArbitrationId());
        if (e == null) throw new BusinessException("仲裁记录不存在");
        if (!Objects.equals(normalizeStatus(e.getStatus()), PROCESSING)) {
            throw new BusinessException("当前状态不允许完结");
        }

        String decisionRemark = StringUtils.hasText(dto.getDecisionRemark()) ? dto.getDecisionRemark().trim() : "";
        if (!StringUtils.hasText(decisionRemark)) throw new BusinessException("decisionRemark不能为空");

        List<ArbitrationSupplementRequestEntity> pending = supplementRequestService.listPendingByArbitrationId(e.getId());
        if (!pending.isEmpty()) {
            expireAllPending(e.getId(), handlerId, "案件完结前自动结转待补证请求");
        }

        e.setStatus(COMPLETED);
        e.setResult(decisionRemark);
        e.setHandlerId(handlerId);
        e.setUpdateTime(LocalDateTime.now());
        if (!updateById(e)) throw new BusinessException("完结仲裁申请失败");
        log(e.getId(), handlerId, "COMPLETE", "案件完结：" + decisionRemark);
        return true;
    }

    @Override
    @Transactional
    public Boolean rejectAdminArbitration(ArbitrationRejectDTO dto, Long handlerId) {
        ArbitrationEntity e = getById(dto.getArbitrationId());
        if (e == null) throw new BusinessException("仲裁记录不存在");
        Integer status = normalizeStatus(e.getStatus());
        if (!Arrays.asList(PENDING, PROCESSING).contains(status)) {
            throw new BusinessException("当前状态不允许驳回");
        }
        String rejectReason = StringUtils.hasText(dto.getRejectReason()) ? dto.getRejectReason().trim() : "";
        if (!StringUtils.hasText(rejectReason)) throw new BusinessException("rejectReason不能为空");

        List<ArbitrationSupplementRequestEntity> pending = supplementRequestService.listPendingByArbitrationId(e.getId());
        if (!pending.isEmpty()) {
            expireAllPending(e.getId(), handlerId, "案件驳回前自动结转待补证请求");
        }

        e.setStatus(REJECTED);
        e.setResult("驳回原因：" + rejectReason);
        e.setHandlerId(handlerId);
        e.setUpdateTime(LocalDateTime.now());
        if (!updateById(e)) throw new BusinessException("驳回仲裁申请失败");
        log(e.getId(), handlerId, "REJECT", "案件驳回：" + rejectReason);
        return true;
    }

    @Override
    @Transactional
    public Boolean acceptArbitration(Long id, Long handlerId) {
        return acceptAdminArbitration(id, handlerId);
    }

    @Override
    @Transactional
    public Boolean handleArbitration(Long id, String result, Long handlerId) {
        return handleArbitration(id, result, handlerId, false);
    }

    @Override
    @Transactional
    public Boolean handleArbitration(Long id, String result, Long handlerId, Boolean force) {
        ArbitrationCompleteDTO dto = new ArbitrationCompleteDTO();
        dto.setArbitrationId(id);
        dto.setDecisionRemark(result);
        return completeAdminArbitration(dto, handlerId);
    }

    @Override
    @Transactional
    public Boolean rejectArbitration(Long id, String reason, Long handlerId) {
        ArbitrationRejectDTO dto = new ArbitrationRejectDTO();
        dto.setArbitrationId(id);
        dto.setRejectReason(reason);
        return rejectAdminArbitration(dto, handlerId);
    }

    @Override
    @Transactional
    public Boolean cancelArbitration(Long id, Long applicantId) {
        ArbitrationEntity e = getById(id);
        if (e == null) throw new BusinessException("仲裁记录不存在");
        if (!Objects.equals(e.getApplicantId(), applicantId)) throw new BusinessException("无权限取消该仲裁申请");
        if (!Arrays.asList(PENDING, LEGACY_WAIT_SUPPLEMENT).contains(e.getStatus())) throw new BusinessException("当前状态不允许取消");
        if (Objects.equals(e.getStatus(), LEGACY_WAIT_SUPPLEMENT)) {
            for (ArbitrationSupplementRequestEntity r : supplementRequestService.listPendingByArbitrationId(id)) {
                r.setStatus(SR_CANCELED); r.setUpdateTime(LocalDateTime.now()); supplementRequestService.updateById(r);
            }
        }
        e.setStatus(REJECTED); e.setResult("申请人已取消仲裁申请"); e.setUpdateTime(LocalDateTime.now());
        if (!updateById(e)) throw new BusinessException("取消仲裁申请失败");
        log(id, applicantId, "CANCEL", "用户取消仲裁申请");
        return true;
    }

    @Override
    public ArbitrationStatsVO getArbitrationStats() {
        ArbitrationStatsVO s = new ArbitrationStatsVO();
        s.setPendingCount(Math.toIntExact(count(new QueryWrapper<ArbitrationEntity>().eq("status", PENDING))));
        s.setProcessingCount(Math.toIntExact(count(new QueryWrapper<ArbitrationEntity>().eq("status", PROCESSING))));
        s.setCompletedCount(Math.toIntExact(count(new QueryWrapper<ArbitrationEntity>().eq("status", COMPLETED))));
        s.setRejectedCount(Math.toIntExact(count(new QueryWrapper<ArbitrationEntity>().eq("status", REJECTED))));
        int supplement = Math.toIntExact(count(new QueryWrapper<ArbitrationEntity>().eq("status", LEGACY_WAIT_SUPPLEMENT)));
        s.setProcessingCount(s.getProcessingCount() + supplement);
        int total = Math.toIntExact(count(new QueryWrapper<>()));
        s.setTotalCount(total); s.setTotalCases(total);
        s.setTodayNewCount(Math.toIntExact(count(new QueryWrapper<ArbitrationEntity>().ge("create_time", LocalDate.now()))));
        s.setTodayCount(s.getTodayNewCount());
        s.setUrgentCount(Math.toIntExact(count(new QueryWrapper<ArbitrationEntity>().in("status", Arrays.asList(PENDING, PROCESSING, LEGACY_WAIT_SUPPLEMENT)).le("create_time", LocalDateTime.now().minusDays(3)))));
        double avg = avgHandleDays();
        s.setAvgHandleDays(avg); s.setAvgProcessDays(avg);
        s.setResolvedCount(s.getCompletedCount() + s.getRejectedCount());
        return s;
    }

    @Override
    public ArbitrationStatsVO getUserArbitrationStats(Long userId) {
        ArbitrationStatsVO s = new ArbitrationStatsVO();
        List<ArbitrationEntity> list = this.list(new QueryWrapper<ArbitrationEntity>().eq("applicant_id", userId));
        int pending=0, processing=0, completed=0, rejected=0;
        for (ArbitrationEntity e : list) {
            if (Objects.equals(e.getStatus(), PENDING)) pending++;
            else if (Arrays.asList(PROCESSING, LEGACY_WAIT_SUPPLEMENT).contains(e.getStatus())) processing++;
            else if (Objects.equals(e.getStatus(), COMPLETED)) completed++;
            else if (Objects.equals(e.getStatus(), REJECTED)) rejected++;
        }
        s.setTotalCount(list.size()); s.setPendingCount(pending); s.setProcessingCount(processing); s.setCompletedCount(completed); s.setRejectedCount(rejected); s.setSuccessfulCount(completed);
        int resolved = completed + rejected; s.setSuccessRate(resolved == 0 ? 0.0 : completed * 100.0 / resolved);
        return s;
    }

    @Override
    public IPage<ArbitrationEntity> getUserArbitrationList(Long userId, Integer current, Integer size) {
        QueryWrapper<ArbitrationEntity> q = new QueryWrapper<>();
        q.eq("applicant_id", userId).orderByDesc("create_time");
        return page(new Page<>(current, size), q);
    }

    @Override
    public ArbitrationEntity getUserArbitrationByOrderId(Long userId, Long orderId) {
        QueryWrapper<ArbitrationEntity> q = new QueryWrapper<>();
        q.eq("applicant_id", userId).eq("order_id", orderId).orderByDesc("create_time").last("limit 1");
        return getOne(q, false);
    }

    @Override
    public Boolean checkArbitrationExists(Long orderId, Long applicantId) {
        QueryWrapper<ArbitrationEntity> q = new QueryWrapper<>();
        q.eq("order_id", orderId).eq("applicant_id", applicantId).ne("status", REJECTED);
        return count(q) > 0;
    }

    @Override
    public AdminArbitrationDetailVO getAdminArbitrationDetail(Long arbitrationId) {
        ArbitrationEntity e = getById(arbitrationId);
        if (e == null) throw new BusinessException("仲裁记录不存在");

        Integer status = normalizeStatus(e.getStatus());
        OrderDTO orderDTO = fetchOrder(e.getOrderId());
        ProductDTO productDTO = orderDTO == null ? null : fetchProduct(orderDTO.getProductId());
        List<ArbitrationEvidenceSubmissionEntity> submissions = evidenceSubmissionService.listByArbitrationId(arbitrationId);
        List<ArbitrationLogEntity> logs = arbitrationLogService.getLogsByArbitrationId(arbitrationId);
        List<ArbitrationSupplementRequestEntity> requests = supplementRequestService.listByArbitrationId(arbitrationId);

        AdminArbitrationDetailVO vo = new AdminArbitrationDetailVO();
        vo.setId(e.getId());
        vo.setCaseNumber(buildCaseNumber(e.getId()));
        vo.setStatus(status);
        vo.setStatusLabel(statusLabel(status));
        vo.setReason(e.getReason());
        vo.setReasonLabel(reasonLabel(e.getReason()));
        vo.setCreateTime(e.getCreateTime());
        vo.setUpdateTime(e.getUpdateTime());

        vo.setOrderId(e.getOrderId());
        vo.setOrderNo(orderDTO == null ? null : orderDTO.getOrderNo());
        vo.setOrderAmount(orderDTO == null ? null : orderDTO.getAmount());
        vo.setProductId(orderDTO == null ? null : orderDTO.getProductId());
        vo.setProductName(productDTO == null ? null : productDTO.getTitle());
        vo.setProductPrice(productDTO == null ? null : productDTO.getPrice());
        vo.setProductImage(resolveProductImage(productDTO));

        vo.setApplicantId(e.getApplicantId());
        vo.setApplicantName(resolveUserName(e.getApplicantId()));
        vo.setRespondentId(e.getRespondentId());
        vo.setRespondentName(resolveUserName(e.getRespondentId()));
        vo.setHandlerId(e.getHandlerId());
        vo.setHandlerName(resolveHandlerName(e.getHandlerId()));

        vo.setBuyerClaim(resolveBuyerClaim(e, submissions));
        vo.setSellerClaim(resolveSellerClaim(submissions));
        vo.setPlatformFocus(resolvePlatformFocus(e, orderDTO, requests));
        vo.setArbitrationRequest(firstText(e.getDescription(), reasonLabel(e.getReason())));

        vo.setApplicantEvidence(buildEvidenceByRole(e, submissions, BUYER));
        vo.setRespondentEvidence(buildEvidenceByRole(e, submissions, SELLER));
        vo.setSystemEvidence(buildSystemEvidence(e, orderDTO, productDTO, requests));
        vo.setChatSummary(buildChatSummary(submissions));
        vo.setOrderSnapshot(buildOrderSnapshot(orderDTO));
        vo.setProductSnapshot(buildProductSnapshot(productDTO));
        vo.setTimeline(buildTimeline(logs));

        String result = StringUtils.hasText(e.getResult()) ? e.getResult().trim() : "";
        vo.setDecisionRemark(status == COMPLETED ? result : "");
        vo.setRejectReason(status == REJECTED ? stripRejectPrefix(result) : "");

        vo.setCanAccept(status == PENDING);
        vo.setCanComplete(status == PROCESSING);
        vo.setCanReject(status == PENDING || status == PROCESSING);
        vo.setReadonly(status == COMPLETED || status == REJECTED);
        return vo;
    }

    @Override
    @Transactional
    public Boolean requestSupplement(SupplementRequestDTO dto, Long handlerId) {
        ArbitrationEntity e = getById(dto.getArbitrationId());
        if (e == null) throw new BusinessException("仲裁记录不存在");
        if (!Arrays.asList(PENDING, PROCESSING, LEGACY_WAIT_SUPPLEMENT).contains(e.getStatus())) throw new BusinessException("当前仲裁状态不允许发起补证");
        String target = normalizeTarget(dto.getTargetParty());
        int hours = dto.getDueHours() == null || dto.getDueHours() <= 0 ? 48 : dto.getDueHours();
        ArbitrationSupplementRequestEntity r = new ArbitrationSupplementRequestEntity();
        r.setArbitrationId(e.getId()); r.setRequestedBy(handlerId); r.setTargetParty(target); r.setRequiredItems(dto.getRequiredItems()); r.setRemark(dto.getRemark()); r.setDueTime(LocalDateTime.now().plusHours(hours)); r.setStatus(SR_PENDING);
        if (!supplementRequestService.save(r)) throw new BusinessException("发起补证失败");
        if (Objects.equals(e.getStatus(), PENDING) || Objects.equals(e.getStatus(), LEGACY_WAIT_SUPPLEMENT)) {
            e.setStatus(PROCESSING);
        }
        e.setHandlerId(handlerId); e.setUpdateTime(LocalDateTime.now()); updateById(e);
        log(e.getId(), handlerId, "SUPPLEMENT_REQUEST", "发起补证，目标=" + target + "，要求=" + dto.getRequiredItems());
        return true;
    }

    @Override
    @Transactional
    public Boolean submitSupplement(SupplementSubmitDTO dto, Long submitterId) {
        ArbitrationEntity e = getById(dto.getArbitrationId());
        if (e == null) throw new BusinessException("仲裁记录不存在");
        if (!Objects.equals(submitterId, e.getApplicantId()) && !Objects.equals(submitterId, e.getRespondentId())) throw new BusinessException("无权限提交该仲裁补证");
        if (!hasContent(dto)) throw new BusinessException("请至少提交一项补证内容");
        String role = resolveRole(e, submitterId);
        ArbitrationSupplementRequestEntity req = resolveReq(e.getId(), dto.getSupplementRequestId(), role);
        boolean late = req.getDueTime() != null && LocalDateTime.now().isAfter(req.getDueTime());
        ArbitrationEvidenceSubmissionEntity sub = new ArbitrationEvidenceSubmissionEntity();
        sub.setArbitrationId(e.getId()); sub.setSupplementRequestId(req.getId()); sub.setSubmitterId(submitterId); sub.setSubmitterRole(role);
        sub.setClaimText(dto.getClaim()); sub.setFactText(dto.getFacts()); sub.setEvidenceUrls(toJson(dto.getEvidenceUrls())); sub.setNote(dto.getNote()); sub.setIsLate(late ? 1 : 0);
        if (!evidenceSubmissionService.save(sub)) throw new BusinessException("补证提交失败");
        log(e.getId(), submitterId, "SUPPLEMENT_SUBMIT", late ? "补证已提交（逾期）" : "补证已提交");
        if (reqSatisfied(req)) {
            req.setStatus(SR_SUBMITTED); req.setUpdateTime(LocalDateTime.now()); supplementRequestService.updateById(req);
            log(e.getId(), submitterId, "SUPPLEMENT_COMPLETE", "补证请求已满足");
        }
        if (supplementRequestService.listPendingByArbitrationId(e.getId()).isEmpty() && Objects.equals(e.getStatus(), LEGACY_WAIT_SUPPLEMENT)) {
            e.setStatus(PROCESSING); e.setUpdateTime(LocalDateTime.now()); updateById(e);
        }
        return true;
    }

    @Override
    @Transactional
    public Boolean expireSupplementRequest(Long requestId, Long operatorId) {
        ArbitrationSupplementRequestEntity r = supplementRequestService.getById(requestId);
        if (r == null) throw new BusinessException("补证请求不存在");
        if (!Objects.equals(r.getStatus(), SR_PENDING)) throw new BusinessException("补证请求已处理，无需结转");
        r.setStatus(SR_EXPIRED); r.setUpdateTime(LocalDateTime.now()); supplementRequestService.updateById(r);
        ArbitrationEntity e = getById(r.getArbitrationId());
        if (e != null && supplementRequestService.listPendingByArbitrationId(e.getId()).isEmpty() && Objects.equals(e.getStatus(), LEGACY_WAIT_SUPPLEMENT)) {
            e.setStatus(PROCESSING); e.setUpdateTime(LocalDateTime.now()); updateById(e);
        }
        log(r.getArbitrationId(), operatorId, "SUPPLEMENT_EXPIRED", "补证请求超时，按不利事实继续裁决");
        return true;
    }

    private void expireAllPending(Long arbitrationId, Long operatorId, String prefix) {
        for (ArbitrationSupplementRequestEntity r : supplementRequestService.listPendingByArbitrationId(arbitrationId)) {
            r.setStatus(SR_EXPIRED); r.setUpdateTime(LocalDateTime.now()); supplementRequestService.updateById(r);
            log(arbitrationId, operatorId, "SUPPLEMENT_EXPIRED", prefix + "，requestId=" + r.getId());
        }
    }

    private AdminArbitrationListItemVO toAdminListItem(ArbitrationEntity e) {
        AdminArbitrationListItemVO vo = new AdminArbitrationListItemVO();
        OrderDTO orderDTO = fetchOrder(e.getOrderId());
        Integer status = normalizeStatus(e.getStatus());
        vo.setId(e.getId());
        vo.setCaseNumber(buildCaseNumber(e.getId()));
        vo.setStatus(status);
        vo.setStatusLabel(statusLabel(status));
        vo.setReason(e.getReason());
        vo.setReasonLabel(reasonLabel(e.getReason()));
        vo.setTitle(firstText(e.getDescription(), vo.getReasonLabel()));
        vo.setDescription(e.getDescription());
        vo.setResult(e.getResult());
        vo.setOrderId(e.getOrderId());
        vo.setOrderNo(orderDTO == null ? null : orderDTO.getOrderNo());
        vo.setOrderAmount(orderDTO == null ? null : orderDTO.getAmount());
        vo.setApplicantId(e.getApplicantId());
        vo.setApplicantName(resolveUserName(e.getApplicantId()));
        vo.setRespondentId(e.getRespondentId());
        vo.setRespondentName(resolveUserName(e.getRespondentId()));
        vo.setHandlerId(e.getHandlerId());
        vo.setHandlerName(resolveHandlerName(e.getHandlerId()));
        vo.setCreateTime(e.getCreateTime());
        vo.setUpdateTime(e.getUpdateTime());
        return vo;
    }

    private List<ArbitrationEvidenceVO> buildEvidenceByRole(ArbitrationEntity arbitration,
                                                             List<ArbitrationEvidenceSubmissionEntity> submissions,
                                                             String role) {
        List<ArbitrationEvidenceVO> evidenceList = new ArrayList<>();
        List<ArbitrationEvidenceSubmissionEntity> roleSubs = submissions.stream()
                .filter(x -> role.equalsIgnoreCase(String.valueOf(x.getSubmitterRole())))
                .sorted(Comparator.comparing(ArbitrationEvidenceSubmissionEntity::getCreateTime,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        for (ArbitrationEvidenceSubmissionEntity sub : roleSubs) {
            if (StringUtils.hasText(sub.getClaimText())) {
                ArbitrationEvidenceVO claim = new ArbitrationEvidenceVO();
                claim.setId(sub.getId());
                claim.setType("text");
                claim.setTitle("主张说明");
                claim.setDescription(sub.getClaimText());
                claim.setCreateTime(sub.getCreateTime());
                evidenceList.add(claim);
            }
            if (StringUtils.hasText(sub.getFactText())) {
                ArbitrationEvidenceVO fact = new ArbitrationEvidenceVO();
                fact.setId(sub.getId());
                fact.setType("text");
                fact.setTitle("事实补充");
                fact.setDescription(sub.getFactText());
                fact.setCreateTime(sub.getCreateTime());
                evidenceList.add(fact);
            }
            List<String> urls = parseJson(sub.getEvidenceUrls());
            for (int i = 0; i < urls.size(); i++) {
                String url = urls.get(i);
                long baseId = sub.getId() == null ? 0L : sub.getId();
                ArbitrationEvidenceVO image = new ArbitrationEvidenceVO();
                image.setId(baseId * 100 + i + 1);
                image.setType("image");
                image.setTitle("证据图片");
                image.setDescription(StringUtils.hasText(sub.getNote()) ? sub.getNote() : "提交图片证据");
                image.setUrl(url);
                image.setThumbnail(url);
                image.setCreateTime(sub.getCreateTime());
                evidenceList.add(image);
            }
        }

        if (evidenceList.isEmpty() && BUYER.equals(role) && StringUtils.hasText(arbitration.getEvidence())) {
            List<String> urls = parseJson(arbitration.getEvidence());
            for (int i = 0; i < urls.size(); i++) {
                long arbitrationId = arbitration.getId() == null ? 0L : arbitration.getId();
                ArbitrationEvidenceVO image = new ArbitrationEvidenceVO();
                image.setId(arbitrationId * 1000 + i + 1);
                image.setType("image");
                image.setTitle("初始证据");
                image.setDescription("申请仲裁时上传");
                image.setUrl(urls.get(i));
                image.setThumbnail(urls.get(i));
                image.setCreateTime(arbitration.getCreateTime());
                evidenceList.add(image);
            }
        }
        return evidenceList;
    }

    private List<ArbitrationEvidenceVO> buildSystemEvidence(ArbitrationEntity arbitration,
                                                            OrderDTO orderDTO,
                                                            ProductDTO productDTO,
                                                            List<ArbitrationSupplementRequestEntity> requests) {
        List<ArbitrationEvidenceVO> list = new ArrayList<>();

        ArbitrationEvidenceVO statusEvidence = new ArbitrationEvidenceVO();
        statusEvidence.setId(arbitration.getId() * 10 + 1);
        statusEvidence.setType("system");
        statusEvidence.setTitle("平台状态记录");
        statusEvidence.setDescription("案件当前状态：" + statusLabel(normalizeStatus(arbitration.getStatus())));
        statusEvidence.setCreateTime(arbitration.getUpdateTime() == null ? arbitration.getCreateTime() : arbitration.getUpdateTime());
        list.add(statusEvidence);

        if (orderDTO != null) {
            ArbitrationEvidenceVO orderEvidence = new ArbitrationEvidenceVO();
            orderEvidence.setId(arbitration.getId() * 10 + 2);
            orderEvidence.setType("system");
            orderEvidence.setTitle("订单快照");
            orderEvidence.setDescription("订单号：" + firstText(orderDTO.getOrderNo(), "-")
                    + "，金额：" + (orderDTO.getAmount() == null ? "-" : orderDTO.getAmount().toPlainString()));
            orderEvidence.setCreateTime(orderDTO.getUpdateTime() == null ? orderDTO.getCreateTime() : orderDTO.getUpdateTime());
            list.add(orderEvidence);
        }

        if (productDTO != null) {
            ArbitrationEvidenceVO productEvidence = new ArbitrationEvidenceVO();
            productEvidence.setId(arbitration.getId() * 10 + 3);
            productEvidence.setType("system");
            productEvidence.setTitle("商品快照");
            productEvidence.setDescription(firstText(productDTO.getTitle(), "商品信息") + "，标价："
                    + (productDTO.getPrice() == null ? "-" : productDTO.getPrice().toPlainString()));
            productEvidence.setUrl(resolveProductImage(productDTO));
            productEvidence.setThumbnail(resolveProductImage(productDTO));
            productEvidence.setCreateTime(productDTO.getUpdateTime() == null ? productDTO.getCreateTime() : productDTO.getUpdateTime());
            list.add(productEvidence);
        }

        for (ArbitrationSupplementRequestEntity request : requests) {
            ArbitrationEvidenceVO requestEvidence = new ArbitrationEvidenceVO();
            requestEvidence.setId(request.getId());
            requestEvidence.setType("system");
            requestEvidence.setTitle("补证请求");
            requestEvidence.setDescription("对象：" + request.getTargetParty() + "，要求：" + request.getRequiredItems());
            requestEvidence.setCreateTime(request.getCreateTime());
            list.add(requestEvidence);
        }
        return list;
    }

    private List<ArbitrationChatSummaryVO> buildChatSummary(List<ArbitrationEvidenceSubmissionEntity> submissions) {
        List<ArbitrationChatSummaryVO> list = new ArrayList<>();
        long seed = 1L;
        for (ArbitrationEvidenceSubmissionEntity sub : submissions) {
            String speaker = BUYER.equalsIgnoreCase(sub.getSubmitterRole()) ? "买家" :
                    (SELLER.equalsIgnoreCase(sub.getSubmitterRole()) ? "卖家" : "平台");
            if (StringUtils.hasText(sub.getClaimText())) {
                ArbitrationChatSummaryVO item = new ArbitrationChatSummaryVO();
                item.setId(seed++);
                item.setSpeaker(speaker);
                item.setTime(sub.getCreateTime());
                item.setContent(sub.getClaimText());
                list.add(item);
            }
            if (StringUtils.hasText(sub.getFactText())) {
                ArbitrationChatSummaryVO item = new ArbitrationChatSummaryVO();
                item.setId(seed++);
                item.setSpeaker(speaker);
                item.setTime(sub.getCreateTime());
                item.setContent(sub.getFactText());
                list.add(item);
            }
        }
        if (list.isEmpty()) {
            ArbitrationChatSummaryVO item = new ArbitrationChatSummaryVO();
            item.setId(1L);
            item.setSpeaker("平台");
            item.setTime(LocalDateTime.now());
            item.setContent("暂无可提取的聊天摘要，建议结合订单与证据材料判定。");
            list.add(item);
        }
        return list;
    }

    private List<ArbitrationTimelineVO> buildTimeline(List<ArbitrationLogEntity> logs) {
        List<ArbitrationTimelineVO> timeline = new ArrayList<>();
        for (ArbitrationLogEntity logEntity : logs) {
            ArbitrationTimelineVO item = new ArbitrationTimelineVO();
            item.setId(logEntity.getId());
            item.setTime(logEntity.getCreateTime());
            item.setTitle(timelineTitle(logEntity.getAction()));
            item.setDescription(firstText(logEntity.getRemark(), "无备注"));
            item.setColor(timelineColor(logEntity.getAction()));
            timeline.add(item);
        }
        timeline.sort(Comparator.comparing(ArbitrationTimelineVO::getTime, Comparator.nullsLast(Comparator.naturalOrder())));
        return timeline;
    }

    private Map<String, Object> buildOrderSnapshot(OrderDTO orderDTO) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (orderDTO == null) {
            map.put("note", "暂无订单快照");
            return map;
        }
        map.put("orderId", orderDTO.getId());
        map.put("orderNo", orderDTO.getOrderNo());
        map.put("status", orderDTO.getStatus());
        map.put("amount", orderDTO.getAmount());
        map.put("buyerId", orderDTO.getBuyerId());
        map.put("sellerId", orderDTO.getSellerId());
        map.put("createTime", orderDTO.getCreateTime());
        map.put("updateTime", orderDTO.getUpdateTime());
        return map;
    }

    private Map<String, Object> buildProductSnapshot(ProductDTO productDTO) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (productDTO == null) {
            map.put("description", "暂无商品快照");
            return map;
        }
        map.put("productId", productDTO.getId());
        map.put("title", productDTO.getTitle());
        map.put("description", productDTO.getDescription());
        map.put("price", productDTO.getPrice());
        map.put("status", productDTO.getStatus());
        map.put("imageUrls", productDTO.getImageUrls());
        return map;
    }

    private Integer normalizeStatus(Integer status) {
        if (Objects.equals(status, LEGACY_WAIT_SUPPLEMENT)) {
            return PROCESSING;
        }
        return status == null ? PENDING : status;
    }

    private String statusLabel(Integer status) {
        return ArbitrationStatus.getByCode(normalizeStatus(status)).getName();
    }

    private String reasonLabel(String reason) {
        return ArbitrationReason.getByCode(reason).getDescription();
    }

    private String buildCaseNumber(Long id) {
        return "ARB" + String.format("%06d", id == null ? 0L : id);
    }

    private String resolveUserName(Long userId) {
        if (userId == null) return "未知用户";
        try {
            Result<UserDTO> result = userFeignClient.getUserById(userId);
            if (result != null && result.isSuccess() && result.getData() != null) {
                UserDTO user = result.getData();
                return firstText(user.getNickname(), user.getUsername(), "用户" + userId);
            }
        } catch (Exception e) {
            log.warn("fetch user failed, userId={}", userId, e);
        }
        return "用户" + userId;
    }

    private String resolveHandlerName(Long handlerId) {
        if (handlerId == null) return "待分配";
        return resolveUserName(handlerId);
    }

    private String resolveBuyerClaim(ArbitrationEntity arbitration, List<ArbitrationEvidenceSubmissionEntity> submissions) {
        Optional<ArbitrationEvidenceSubmissionEntity> latestBuyer = submissions.stream()
                .filter(x -> BUYER.equalsIgnoreCase(x.getSubmitterRole()))
                .max(Comparator.comparing(ArbitrationEvidenceSubmissionEntity::getCreateTime,
                        Comparator.nullsLast(Comparator.naturalOrder())));
        if (latestBuyer.isPresent()) {
            return firstText(latestBuyer.get().getClaimText(), latestBuyer.get().getFactText(),
                    arbitration.getDescription(), "暂无买家主张");
        }
        return firstText(arbitration.getDescription(), "暂无买家主张");
    }

    private String resolveSellerClaim(List<ArbitrationEvidenceSubmissionEntity> submissions) {
        Optional<ArbitrationEvidenceSubmissionEntity> latestSeller = submissions.stream()
                .filter(x -> SELLER.equalsIgnoreCase(x.getSubmitterRole()))
                .max(Comparator.comparing(ArbitrationEvidenceSubmissionEntity::getCreateTime,
                        Comparator.nullsLast(Comparator.naturalOrder())));
        if (!latestSeller.isPresent()) return "暂无卖家主张";
        return firstText(latestSeller.get().getClaimText(), latestSeller.get().getFactText(), "暂无卖家主张");
    }

    private String resolvePlatformFocus(ArbitrationEntity arbitration, OrderDTO orderDTO,
                                        List<ArbitrationSupplementRequestEntity> requests) {
        StringBuilder builder = new StringBuilder();
        builder.append("争议类型：").append(reasonLabel(arbitration.getReason()));
        if (orderDTO != null && orderDTO.getAmount() != null) {
            builder.append("；订单金额：").append(orderDTO.getAmount().toPlainString());
        }
        if (!requests.isEmpty()) {
            builder.append("；当前存在补证请求 ").append(requests.size()).append(" 条");
        }
        return builder.toString();
    }

    private String resolveProductImage(ProductDTO productDTO) {
        if (productDTO == null) return "";
        if (StringUtils.hasText(productDTO.getCoverImage())) return productDTO.getCoverImage();
        if (productDTO.getImageUrls() != null && !productDTO.getImageUrls().isEmpty()) {
            return productDTO.getImageUrls().get(0);
        }
        return "";
    }

    private String stripRejectPrefix(String result) {
        if (!StringUtils.hasText(result)) return "";
        String text = result.trim();
        String prefix = "驳回原因：";
        return text.startsWith(prefix) ? text.substring(prefix.length()) : text;
    }

    private String timelineTitle(String action) {
        if (!StringUtils.hasText(action)) return "处理记录";
        String a = action.trim().toUpperCase(Locale.ROOT);
        if ("SUBMIT".equals(a)) return "发起仲裁";
        if ("ACCEPT".equals(a)) return "平台受理";
        if ("SUPPLEMENT_SUBMIT".equals(a) || "SUPPLEMENT_COMPLETE".equals(a) || "UPDATE".equals(a)) return "补充证据";
        if ("PROCESSING".equals(a) || "SUPPLEMENT_REQUEST".equals(a) || "SUPPLEMENT_EXPIRED".equals(a)) return "平台处理中";
        if ("COMPLETE".equals(a) || "RESOLVE".equals(a)) return "案件完结";
        if ("REJECT".equals(a) || "CANCEL".equals(a)) return "案件驳回";
        return "处理记录";
    }

    private String timelineColor(String action) {
        if (!StringUtils.hasText(action)) return "#909399";
        String a = action.trim().toUpperCase(Locale.ROOT);
        if ("SUBMIT".equals(a)) return "#E6A23C";
        if ("ACCEPT".equals(a) || "PROCESSING".equals(a) || "SUPPLEMENT_REQUEST".equals(a)
                || "SUPPLEMENT_SUBMIT".equals(a) || "SUPPLEMENT_COMPLETE".equals(a) || "SUPPLEMENT_EXPIRED".equals(a)) {
            return "#409EFF";
        }
        if ("COMPLETE".equals(a) || "RESOLVE".equals(a)) return "#67C23A";
        if ("REJECT".equals(a) || "CANCEL".equals(a)) return "#F56C6C";
        return "#909399";
    }

    private String firstText(String... values) {
        if (values == null) return "";
        for (String value : values) {
            if (StringUtils.hasText(value)) return value.trim();
        }
        return "";
    }

    private ArbitrationSupplementRequestEntity resolveReq(Long arbitrationId, Long requestId, String role) {
        if (requestId != null) {
            ArbitrationSupplementRequestEntity r = supplementRequestService.getById(requestId);
            if (r == null || !Objects.equals(r.getArbitrationId(), arbitrationId)) throw new BusinessException("补证请求不存在");
            if (!Objects.equals(r.getStatus(), SR_PENDING)) throw new BusinessException("该补证请求已结束");
            if (!targetMatch(r.getTargetParty(), role)) throw new BusinessException("当前用户不在该补证请求对象中");
            return r;
        }
        return supplementRequestService.listPendingByArbitrationId(arbitrationId).stream().filter(x -> targetMatch(x.getTargetParty(), role)).findFirst().orElseThrow(() -> new BusinessException("当前没有待提交的补证请求"));
    }

    private boolean reqSatisfied(ArbitrationSupplementRequestEntity req) {
        List<ArbitrationEvidenceSubmissionEntity> list = evidenceSubmissionService.listByRequestId(req.getId());
        boolean b = list.stream().anyMatch(x -> BUYER.equalsIgnoreCase(x.getSubmitterRole()));
        boolean s = list.stream().anyMatch(x -> SELLER.equalsIgnoreCase(x.getSubmitterRole()));
        String t = normalizeTarget(req.getTargetParty());
        if ("BOTH".equals(t)) return b && s;
        if ("BUYER".equals(t)) return b;
        return s;
    }

    private boolean targetMatch(String target, String role) { String t = normalizeTarget(target); return "BOTH".equals(t) ? (BUYER.equals(role) || SELLER.equals(role)) : t.equals(role); }

    private String normalizeTarget(String target) {
        String t = StringUtils.hasText(target) ? target.trim().toUpperCase(Locale.ROOT) : "BOTH";
        if (!Arrays.asList("BUYER", "SELLER", "BOTH").contains(t)) throw new BusinessException("补证对象只支持 BUYER / SELLER / BOTH");
        return t;
    }

    private boolean hasContent(SupplementSubmitDTO dto) {
        return StringUtils.hasText(dto.getClaim()) || StringUtils.hasText(dto.getFacts()) || (dto.getEvidenceUrls() != null && !dto.getEvidenceUrls().isEmpty()) || StringUtils.hasText(dto.getNote());
    }

    private String resolveRole(ArbitrationEntity e, Long userId) {
        OrderDTO o = fetchOrder(e.getOrderId());
        if (o != null) {
            if (Objects.equals(o.getBuyerId(), userId)) return BUYER;
            if (Objects.equals(o.getSellerId(), userId)) return SELLER;
        }
        return Objects.equals(userId, e.getApplicantId()) ? BUYER : SELLER;
    }

    private ArbitrationSupplementRequestVO toRequestVO(ArbitrationSupplementRequestEntity e) {
        ArbitrationSupplementRequestVO v = new ArbitrationSupplementRequestVO();
        v.setId(e.getId()); v.setArbitrationId(e.getArbitrationId()); v.setRequestedBy(e.getRequestedBy()); v.setTargetParty(e.getTargetParty());
        v.setRequiredItems(e.getRequiredItems()); v.setRemark(e.getRemark()); v.setDueTime(e.getDueTime()); v.setStatus(e.getStatus()); v.setStatusDesc(reqStatusDesc(e.getStatus())); v.setCreateTime(e.getCreateTime());
        return v;
    }

    private String reqStatusDesc(Integer s) { if (Objects.equals(s, SR_PENDING)) return "待补证"; if (Objects.equals(s, SR_SUBMITTED)) return "已满足"; if (Objects.equals(s, SR_EXPIRED)) return "已超时"; if (Objects.equals(s, SR_CANCELED)) return "已取消"; return "未知"; }

    private List<ArbitrationEvidenceBundleVO> buildBundles(ArbitrationEntity a, List<ArbitrationEvidenceSubmissionEntity> subs) {
        List<ArbitrationEvidenceSubmissionEntity> list = new ArrayList<>(subs == null ? Collections.emptyList() : subs);
        if (list.isEmpty() && StringUtils.hasText(a.getEvidence())) {
            ArbitrationEvidenceSubmissionEntity legacy = new ArbitrationEvidenceSubmissionEntity();
            legacy.setId(0L); legacy.setArbitrationId(a.getId()); legacy.setSubmitterId(a.getApplicantId()); legacy.setSubmitterRole(BUYER);
            legacy.setClaimText(a.getReason()); legacy.setFactText(a.getDescription()); legacy.setEvidenceUrls(normalizeEvidenceJson(a.getEvidence())); legacy.setIsLate(0); legacy.setCreateTime(a.getCreateTime());
            list.add(legacy);
        }
        Map<String, List<ArbitrationEvidenceSubmissionEntity>> g = list.stream().collect(Collectors.groupingBy(x -> {
            String r = StringUtils.hasText(x.getSubmitterRole()) ? x.getSubmitterRole().toUpperCase(Locale.ROOT) : SYSTEM;
            return (BUYER.equals(r) || SELLER.equals(r) || SYSTEM.equals(r)) ? r : SYSTEM;
        }, LinkedHashMap::new, Collectors.toList()));
        List<ArbitrationEvidenceBundleVO> bundles = new ArrayList<>();
        for (String r : Arrays.asList(BUYER, SELLER, SYSTEM)) {
            ArbitrationEvidenceBundleVO b = new ArbitrationEvidenceBundleVO(); b.setRole(r);
            b.setSubmissions(g.getOrDefault(r, Collections.emptyList()).stream().map(this::toSubVO).collect(Collectors.toList()));
            bundles.add(b);
        }
        return bundles;
    }

    private ArbitrationEvidenceSubmissionVO toSubVO(ArbitrationEvidenceSubmissionEntity e) {
        ArbitrationEvidenceSubmissionVO v = new ArbitrationEvidenceSubmissionVO();
        v.setId(e.getId()); v.setSupplementRequestId(e.getSupplementRequestId()); v.setSubmitterId(e.getSubmitterId()); v.setSubmitterRole(e.getSubmitterRole());
        v.setClaim(e.getClaimText()); v.setFacts(e.getFactText()); v.setEvidenceUrls(parseJson(e.getEvidenceUrls())); v.setNote(e.getNote()); v.setLate(Objects.equals(e.getIsLate(), 1)); v.setCreateTime(e.getCreateTime());
        return v;
    }

    private ArbitrationSystemContextVO buildSystem(ArbitrationEntity a) {
        ArbitrationSystemContextVO v = new ArbitrationSystemContextVO();
        Map<String, Object> order = new LinkedHashMap<>();
        Map<String, Object> product = new LinkedHashMap<>();
        Map<String, Object> chat = new LinkedHashMap<>();
        OrderDTO o = fetchOrder(a.getOrderId());
        if (o != null) {
            order.put("orderId", o.getId()); order.put("orderNo", o.getOrderNo()); order.put("status", o.getStatus()); order.put("buyerId", o.getBuyerId()); order.put("sellerId", o.getSellerId()); order.put("amount", o.getAmount()); order.put("createTime", o.getCreateTime()); order.put("updateTime", o.getUpdateTime()); order.put("snapshotAt", LocalDateTime.now());
            ProductDTO p = fetchProduct(o.getProductId());
            if (p != null) {
                product.put("productId", p.getId()); product.put("title", p.getTitle()); product.put("description", p.getDescription()); product.put("price", p.getPrice()); product.put("sellerId", p.getSellerId()); product.put("imageUrls", p.getImageUrls()); product.put("status", p.getStatus()); product.put("snapshotAt", LocalDateTime.now());
            }
            LocalDateTime start = o.getCreateTime() == null ? LocalDateTime.now().minusDays(7) : o.getCreateTime().minusDays(1);
            chat.put("scope", "ORDER_CONVERSATION_WINDOW"); chat.put("windowStart", start); chat.put("windowEnd", LocalDateTime.now()); chat.put("source", "MESSAGE_SERVICE_PENDING_INTEGRATION"); chat.put("note", "当前版本仅提供会话范围元信息，聊天正文将由消息服务聚合接口补齐");
        } else {
            order.put("snapshotAt", LocalDateTime.now()); order.put("note", "订单快照拉取失败或订单不存在");
            chat.put("scope", "ORDER_CONVERSATION_WINDOW"); chat.put("source", "MESSAGE_SERVICE_PENDING_INTEGRATION"); chat.put("note", "缺少订单信息，无法计算会话时间窗");
        }
        v.setOrderSnapshot(order); v.setProductSnapshot(product); v.setChatContext(chat);
        return v;
    }

    private OrderDTO fetchOrder(Long orderId) {
        if (orderId == null) return null;
        try {
            Result<OrderDTO> r = tradeFeignClient.getOrderById(orderId);
            if (r != null && r.isSuccess()) return r.getData();
        } catch (Exception e) { log.warn("fetch order failed, orderId={}", orderId, e); }
        return null;
    }

    private ProductDTO fetchProduct(Long productId) {
        if (productId == null) return null;
        try {
            Result<ProductDTO> r = productFeignClient.getProductById(productId);
            if (r != null && r.isSuccess()) return r.getData();
        } catch (Exception e) { log.warn("fetch product failed, productId={}", productId, e); }
        return null;
    }

    private void log(Long arbitrationId, Long operatorId, String action, String remark) {
        ArbitrationLogEntity e = new ArbitrationLogEntity();
        e.setArbitrationId(arbitrationId); e.setOperatorId(operatorId); e.setAction(action); e.setRemark(remark); arbitrationLogService.save(e);
    }

    private double avgHandleDays() {
        List<ArbitrationEntity> list = this.list(new QueryWrapper<ArbitrationEntity>().eq("status", COMPLETED));
        if (list.isEmpty()) return 0.0;
        double total = 0.0; int valid = 0;
        for (ArbitrationEntity e : list) {
            if (e.getCreateTime() == null || e.getUpdateTime() == null) continue;
            long hours = ChronoUnit.HOURS.between(e.getCreateTime(), e.getUpdateTime());
            total += Math.max(0, hours) / 24.0; valid++;
        }
        return valid == 0 ? 0.0 : Math.round((total / valid) * 100.0) / 100.0;
    }

    private String toJson(List<String> arr) {
        List<String> v = arr == null ? Collections.emptyList() : arr.stream().filter(StringUtils::hasText).map(String::trim).collect(Collectors.toList());
        try { return om.writeValueAsString(v); } catch (Exception e) { return "[]"; }
    }

    private List<String> parseJson(String s) {
        if (!StringUtils.hasText(s)) return Collections.emptyList();
        try { return om.readValue(s, new TypeReference<List<String>>() {}); } catch (Exception e) { return Collections.singletonList(s); }
    }

    private String normalizeEvidenceJson(String evidence) { return StringUtils.hasText(evidence) ? toJson(parseJson(evidence)) : "[]"; }
}
