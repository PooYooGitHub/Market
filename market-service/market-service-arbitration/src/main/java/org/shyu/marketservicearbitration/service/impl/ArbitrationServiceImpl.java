package org.shyu.marketservicearbitration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.shyu.marketapiproduct.dto.ProductDTO;
import org.shyu.marketapiproduct.feign.ProductFeignClient;
import org.shyu.marketapitrade.dto.OrderDTO;
import org.shyu.marketapitrade.feign.TradeFeignClient;
import org.shyu.marketcommon.exception.BusinessException;
import org.shyu.marketcommon.result.Result;
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
    private static final int WAIT_SUPPLEMENT = 4;

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
            if (Objects.equals(status, PROCESSING)) q.in("status", Arrays.asList(PROCESSING, WAIT_SUPPLEMENT));
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
    @Transactional
    public Boolean acceptArbitration(Long id, Long handlerId) {
        ArbitrationEntity e = getById(id);
        if (e == null) throw new BusinessException("仲裁记录不存在");
        if (!Objects.equals(e.getStatus(), PENDING)) throw new BusinessException("该仲裁申请已被处理，无法重复受理");
        e.setStatus(PROCESSING); e.setHandlerId(handlerId); e.setUpdateTime(LocalDateTime.now());
        if (!updateById(e)) throw new BusinessException("受理仲裁申请失败");
        log(id, handlerId, "ACCEPT", "管理员受理仲裁申请");
        return true;
    }

    @Override
    @Transactional
    public Boolean handleArbitration(Long id, String result, Long handlerId) { return handleArbitration(id, result, handlerId, false); }

    @Override
    @Transactional
    public Boolean handleArbitration(Long id, String result, Long handlerId, Boolean force) {
        ArbitrationEntity e = getById(id);
        if (e == null) throw new BusinessException("仲裁记录不存在");
        if (!Arrays.asList(PROCESSING, WAIT_SUPPLEMENT).contains(e.getStatus())) throw new BusinessException("该仲裁申请当前状态不允许完结");
        List<ArbitrationSupplementRequestEntity> pending = supplementRequestService.listPendingByArbitrationId(id);
        if (!pending.isEmpty() && !Boolean.TRUE.equals(force)) throw new BusinessException("当前仍有待补证请求，若需强制完结请传 force=true");
        if (!pending.isEmpty()) expireAllPending(id, handlerId, "强制完结前自动结转待补证请求");
        e.setStatus(COMPLETED); e.setResult(result); e.setHandlerId(handlerId); e.setUpdateTime(LocalDateTime.now());
        if (!updateById(e)) throw new BusinessException("处理仲裁申请失败");
        log(id, handlerId, "RESOLVE", Boolean.TRUE.equals(force) ? "仲裁完结（强制）" : "仲裁完结");
        return true;
    }

    @Override
    @Transactional
    public Boolean rejectArbitration(Long id, String reason, Long handlerId) {
        ArbitrationEntity e = getById(id);
        if (e == null) throw new BusinessException("仲裁记录不存在");
        if (!Arrays.asList(PENDING, PROCESSING, WAIT_SUPPLEMENT).contains(e.getStatus())) throw new BusinessException("该仲裁申请已被处理，无法驳回");
        if (Objects.equals(e.getStatus(), WAIT_SUPPLEMENT)) expireAllPending(id, handlerId, "驳回前自动结转待补证请求");
        e.setStatus(REJECTED); e.setResult("驳回原因：" + reason); e.setHandlerId(handlerId); e.setUpdateTime(LocalDateTime.now());
        if (!updateById(e)) throw new BusinessException("驳回仲裁申请失败");
        log(id, handlerId, "REJECT", "驳回仲裁申请：" + reason);
        return true;
    }

    @Override
    @Transactional
    public Boolean cancelArbitration(Long id, Long applicantId) {
        ArbitrationEntity e = getById(id);
        if (e == null) throw new BusinessException("仲裁记录不存在");
        if (!Objects.equals(e.getApplicantId(), applicantId)) throw new BusinessException("无权限取消该仲裁申请");
        if (!Arrays.asList(PENDING, WAIT_SUPPLEMENT).contains(e.getStatus())) throw new BusinessException("当前状态不允许取消");
        if (Objects.equals(e.getStatus(), WAIT_SUPPLEMENT)) {
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
        int supplement = Math.toIntExact(count(new QueryWrapper<ArbitrationEntity>().eq("status", WAIT_SUPPLEMENT)));
        s.setProcessingCount(s.getProcessingCount() + supplement);
        int total = Math.toIntExact(count(new QueryWrapper<>()));
        s.setTotalCount(total); s.setTotalCases(total);
        s.setTodayNewCount(Math.toIntExact(count(new QueryWrapper<ArbitrationEntity>().ge("create_time", LocalDate.now()))));
        s.setTodayCount(s.getTodayNewCount());
        s.setUrgentCount(Math.toIntExact(count(new QueryWrapper<ArbitrationEntity>().in("status", Arrays.asList(PENDING, PROCESSING, WAIT_SUPPLEMENT)).le("create_time", LocalDateTime.now().minusDays(3)))));
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
            else if (Arrays.asList(PROCESSING, WAIT_SUPPLEMENT).contains(e.getStatus())) processing++;
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
    public ArbitrationAdminDetailVO getAdminArbitrationDetail(Long arbitrationId) {
        ArbitrationEntity e = getById(arbitrationId);
        if (e == null) throw new BusinessException("仲裁记录不存在");
        ArbitrationAdminDetailVO vo = new ArbitrationAdminDetailVO();
        vo.setArbitration(e);
        vo.setLogs(arbitrationLogService.getLogsByArbitrationId(arbitrationId));
        vo.setSupplementRequests(supplementRequestService.listByArbitrationId(arbitrationId).stream().map(this::toRequestVO).collect(Collectors.toList()));
        vo.setEvidenceBundles(buildBundles(e, evidenceSubmissionService.listByArbitrationId(arbitrationId)));
        vo.setSystemContext(buildSystem(e));
        return vo;
    }

    @Override
    @Transactional
    public Boolean requestSupplement(SupplementRequestDTO dto, Long handlerId) {
        ArbitrationEntity e = getById(dto.getArbitrationId());
        if (e == null) throw new BusinessException("仲裁记录不存在");
        if (!Arrays.asList(PENDING, PROCESSING, WAIT_SUPPLEMENT).contains(e.getStatus())) throw new BusinessException("当前仲裁状态不允许发起补证");
        String target = normalizeTarget(dto.getTargetParty());
        int hours = dto.getDueHours() == null || dto.getDueHours() <= 0 ? 48 : dto.getDueHours();
        ArbitrationSupplementRequestEntity r = new ArbitrationSupplementRequestEntity();
        r.setArbitrationId(e.getId()); r.setRequestedBy(handlerId); r.setTargetParty(target); r.setRequiredItems(dto.getRequiredItems()); r.setRemark(dto.getRemark()); r.setDueTime(LocalDateTime.now().plusHours(hours)); r.setStatus(SR_PENDING);
        if (!supplementRequestService.save(r)) throw new BusinessException("发起补证失败");
        e.setStatus(WAIT_SUPPLEMENT); e.setHandlerId(handlerId); e.setUpdateTime(LocalDateTime.now()); updateById(e);
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
        if (supplementRequestService.listPendingByArbitrationId(e.getId()).isEmpty() && Objects.equals(e.getStatus(), WAIT_SUPPLEMENT)) {
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
        if (e != null && supplementRequestService.listPendingByArbitrationId(e.getId()).isEmpty() && Objects.equals(e.getStatus(), WAIT_SUPPLEMENT)) {
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
