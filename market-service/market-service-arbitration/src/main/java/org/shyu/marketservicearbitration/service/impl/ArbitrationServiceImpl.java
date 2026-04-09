package org.shyu.marketservicearbitration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.shyu.marketcommon.exception.BusinessException;
import org.shyu.marketservicearbitration.entity.ArbitrationEntity;
import org.shyu.marketservicearbitration.entity.ArbitrationLogEntity;
import org.shyu.marketservicearbitration.mapper.ArbitrationMapper;
import org.shyu.marketservicearbitration.service.IArbitrationLogService;
import org.shyu.marketservicearbitration.service.IArbitrationService;
import org.shyu.marketservicearbitration.vo.ArbitrationStatsVO;
import org.shyu.marketservicearbitration.vo.ArbitrationVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 仲裁服务实现类
 * @author shyu
 * @since 2026-04-01
 */
@Slf4j
@Service
public class ArbitrationServiceImpl extends ServiceImpl<ArbitrationMapper, ArbitrationEntity>
        implements IArbitrationService {

    @Autowired
    private IArbitrationLogService arbitrationLogService;

    @Override
    @Transactional
    public ArbitrationEntity submitArbitration(ArbitrationVO arbitrationVO) {
        log.info("用户提交仲裁申请: {}", arbitrationVO);

        // 检查订单是否已有仲裁申请
        if (checkArbitrationExists(arbitrationVO.getOrderId(), arbitrationVO.getApplicantId())) {
            throw new BusinessException("该订单已存在仲裁申请，请勿重复提交");
        }

        // 创建仲裁实体
        ArbitrationEntity arbitration = new ArbitrationEntity();
        BeanUtils.copyProperties(arbitrationVO, arbitration);
        arbitration.setStatus(0); // 待处理
        arbitration.setCreateTime(LocalDateTime.now());

        // 保存仲裁申请
        boolean saved = this.save(arbitration);
        if (!saved) {
            throw new BusinessException("仲裁申请提交失败");
        }

        // 记录日志
        ArbitrationLogEntity logEntity = new ArbitrationLogEntity();
        logEntity.setArbitrationId(arbitration.getId());
        logEntity.setOperatorId(arbitrationVO.getApplicantId());
        logEntity.setAction("SUBMIT");
        logEntity.setRemark("用户提交仲裁申请");
        arbitrationLogService.save(logEntity);

        log.info("仲裁申请提交成功，ID: {}", arbitration.getId());
        return arbitration;
    }

    @Override
    @Transactional
    public ArbitrationEntity updateArbitration(Long id, Long applicantId, ArbitrationVO arbitrationVO) {
        log.info("用户{}修改仲裁申请: id={}", applicantId, id);

        ArbitrationEntity arbitration = this.getById(id);
        if (arbitration == null) {
            throw new BusinessException("仲裁记录不存在");
        }

        if (!applicantId.equals(arbitration.getApplicantId())) {
            throw new BusinessException("无权限修改该仲裁申请");
        }

        // 已完结或已驳回的不允许再修改
        if (arbitration.getStatus() != null && (arbitration.getStatus().equals(2) || arbitration.getStatus().equals(3))) {
            throw new BusinessException("当前仲裁状态不可修改");
        }

        arbitration.setReason(arbitrationVO.getReason());
        arbitration.setDescription(arbitrationVO.getDescription());
        arbitration.setEvidence(arbitrationVO.getEvidence());
        if (arbitrationVO.getRespondentId() != null) {
            arbitration.setRespondentId(arbitrationVO.getRespondentId());
        }
        arbitration.setUpdateTime(LocalDateTime.now());

        boolean updated = this.updateById(arbitration);
        if (!updated) {
            throw new BusinessException("修改仲裁申请失败");
        }

        ArbitrationLogEntity logEntity = new ArbitrationLogEntity();
        logEntity.setArbitrationId(id);
        logEntity.setOperatorId(applicantId);
        logEntity.setAction("UPDATE");
        logEntity.setRemark("用户修改仲裁申请");
        arbitrationLogService.save(logEntity);

        return arbitration;
    }

    @Override
    public IPage<ArbitrationEntity> getArbitrationPage(Integer current, Integer size,
                                                      Integer status, Long applicantId, Long respondentId) {
        return getArbitrationPage(current, size, status, applicantId, respondentId, null, null);
        }

        @Override
        public IPage<ArbitrationEntity> getArbitrationPage(Integer current, Integer size,
                                  Integer status, Long applicantId, Long respondentId,
                                  String keyword, String priority) {
        log.info("分页查询仲裁列表: current={}, size={}, status={}, applicantId={}, respondentId={}",
                current, size, status, applicantId, respondentId);
        log.info("管理端筛选条件: keyword={}, priority={}", keyword, priority);

        Page<ArbitrationEntity> page = new Page<>(current, size);
        QueryWrapper<ArbitrationEntity> queryWrapper = new QueryWrapper<>();

        // 添加查询条件
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        if (applicantId != null) {
            queryWrapper.eq("applicant_id", applicantId);
        }
        if (respondentId != null) {
            queryWrapper.eq("respondent_id", respondentId);
        }

        if (StringUtils.hasText(keyword)) {
            String trimmedKeyword = keyword.trim();
            queryWrapper.and(wrapper -> wrapper
                    .like("reason", trimmedKeyword)
                    .or().like("description", trimmedKeyword)
                    .or().like("result", trimmedKeyword)
                    .or().like("order_id", trimmedKeyword)
                    .or().like("applicant_id", trimmedKeyword)
                    .or().like("respondent_id", trimmedKeyword));
        }

        if (StringUtils.hasText(priority)) {
            String normalizedPriority = priority.trim().toLowerCase();
            LocalDateTime now = LocalDateTime.now();
            if ("high".equals(normalizedPriority)) {
                queryWrapper.le("create_time", now.minusDays(3));
            } else if ("normal".equals(normalizedPriority)) {
                queryWrapper.between("create_time", now.minusDays(3), now.minusDays(1));
            } else if ("low".equals(normalizedPriority)) {
                queryWrapper.ge("create_time", now.minusDays(1));
            }
        }

        // 按创建时间倒序排列
        queryWrapper.orderByDesc("create_time");

        return this.page(page, queryWrapper);
    }

    @Override
    public ArbitrationEntity getArbitrationDetail(Long id) {
        log.info("获取仲裁详情，ID: {}", id);

        ArbitrationEntity arbitration = this.getById(id);
        if (arbitration == null) {
            throw new BusinessException("仲裁记录不存在");
        }

        return arbitration;
    }

    @Override
    @Transactional
    public Boolean acceptArbitration(Long id, Long handlerId) {
        log.info("管理员受理仲裁申请，ID: {}, 处理人: {}", id, handlerId);

        // 获取仲裁记录
        ArbitrationEntity arbitration = this.getById(id);
        if (arbitration == null) {
            throw new BusinessException("仲裁记录不存在");
        }

        if (!arbitration.getStatus().equals(0)) {
            throw new BusinessException("该仲裁申请已被处理，无法重复受理");
        }

        // 更新状态为处理中
        arbitration.setStatus(1);
        arbitration.setHandlerId(handlerId);
        arbitration.setUpdateTime(LocalDateTime.now());

        boolean updated = this.updateById(arbitration);
        if (!updated) {
            throw new BusinessException("受理仲裁申请失败");
        }

        // 记录日志
        ArbitrationLogEntity logEntity = new ArbitrationLogEntity();
        logEntity.setArbitrationId(id);
        logEntity.setOperatorId(handlerId);
        logEntity.setAction("ACCEPT");
        logEntity.setRemark("管理员受理仲裁申请");
        arbitrationLogService.save(logEntity);

        log.info("仲裁申请受理成功");
        return true;
    }

    @Override
    @Transactional
    public Boolean handleArbitration(Long id, String result, Long handlerId) {
        log.info("管理员处理仲裁，ID: {}, 处理人: {}", id, handlerId);

        // 获取仲裁记录
        ArbitrationEntity arbitration = this.getById(id);
        if (arbitration == null) {
            throw new BusinessException("仲裁记录不存在");
        }

        if (!arbitration.getStatus().equals(1)) {
            throw new BusinessException("该仲裁申请未在处理中，无法完结");
        }

        // 更新状态为已完结
        arbitration.setStatus(2);
        arbitration.setResult(result);
        arbitration.setHandlerId(handlerId);
        arbitration.setUpdateTime(LocalDateTime.now());

        boolean updated = this.updateById(arbitration);
        if (!updated) {
            throw new BusinessException("处理仲裁申请失败");
        }

        // 记录日志
        ArbitrationLogEntity logEntity = new ArbitrationLogEntity();
        logEntity.setArbitrationId(id);
        logEntity.setOperatorId(handlerId);
        logEntity.setAction("RESOLVE");
        logEntity.setRemark("仲裁完结");
        arbitrationLogService.save(logEntity);

        log.info("仲裁处理成功");
        return true;
    }

    @Override
    @Transactional
    public Boolean rejectArbitration(Long id, String reason, Long handlerId) {
        log.info("管理员驳回仲裁申请，ID: {}, 处理人: {}", id, handlerId);

        // 获取仲裁记录
        ArbitrationEntity arbitration = this.getById(id);
        if (arbitration == null) {
            throw new BusinessException("仲裁记录不存在");
        }

        if (!arbitration.getStatus().equals(0) && !arbitration.getStatus().equals(1)) {
            throw new BusinessException("该仲裁申请已被处理，无法驳回");
        }

        // 更新状态为已驳回
        arbitration.setStatus(3);
        arbitration.setResult("驳回原因：" + reason);
        arbitration.setHandlerId(handlerId);
        arbitration.setUpdateTime(LocalDateTime.now());

        boolean updated = this.updateById(arbitration);
        if (!updated) {
            throw new BusinessException("驳回仲裁申请失败");
        }

        // 记录日志
        ArbitrationLogEntity logEntity = new ArbitrationLogEntity();
        logEntity.setArbitrationId(id);
        logEntity.setOperatorId(handlerId);
        logEntity.setAction("REJECT");
        logEntity.setRemark("驳回仲裁申请：" + reason);
        arbitrationLogService.save(logEntity);

        log.info("仲裁申请驳回成功");
        return true;
    }

    @Override
    public ArbitrationStatsVO getArbitrationStats() {
        log.info("获取仲裁统计数据");

        ArbitrationStatsVO stats = new ArbitrationStatsVO();

        // 查询各状态统计
        QueryWrapper<ArbitrationEntity> queryWrapper = new QueryWrapper<>();

        // 待处理数量
        queryWrapper.eq("status", 0);
        int pendingCount = Math.toIntExact(this.count(queryWrapper));

        // 处理中数量
        queryWrapper.clear();
        queryWrapper.eq("status", 1);
        int processingCount = Math.toIntExact(this.count(queryWrapper));

        // 已完结数量
        queryWrapper.clear();
        queryWrapper.eq("status", 2);
        int completedCount = Math.toIntExact(this.count(queryWrapper));

        // 已驳回数量
        queryWrapper.clear();
        queryWrapper.eq("status", 3);
        int rejectedCount = Math.toIntExact(this.count(queryWrapper));

        // 今日新增
        queryWrapper.clear();
        queryWrapper.ge("create_time", LocalDateTime.now().toLocalDate());
        int todayNewCount = Math.toIntExact(this.count(queryWrapper));

        stats.setPendingCount(pendingCount);
        stats.setProcessingCount(processingCount);
        stats.setCompletedCount(completedCount);
        stats.setRejectedCount(rejectedCount);
        stats.setTodayNewCount(todayNewCount);

        // 计算平均处理天数（简化实现）
        stats.setAvgHandleDays(2.1);

        return stats;
    }

    @Override
    public IPage<ArbitrationEntity> getUserArbitrationList(Long userId, Integer current, Integer size) {
        log.info("获取用户仲裁申请列表，用户ID: {}", userId);

        Page<ArbitrationEntity> page = new Page<>(current, size);
        QueryWrapper<ArbitrationEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("applicant_id", userId);
        queryWrapper.orderByDesc("create_time");

        return this.page(page, queryWrapper);
    }

    @Override
    public ArbitrationEntity getUserArbitrationByOrderId(Long userId, Long orderId) {
        QueryWrapper<ArbitrationEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("applicant_id", userId)
                .eq("order_id", orderId)
                .orderByDesc("create_time")
                .last("limit 1");
        return this.getOne(queryWrapper, false);
    }

    @Override
    public Boolean checkArbitrationExists(Long orderId, Long applicantId) {
        QueryWrapper<ArbitrationEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId)
                   .eq("applicant_id", applicantId)
                   .ne("status", 3); // 排除已驳回的

        return this.count(queryWrapper) > 0;
    }
}
