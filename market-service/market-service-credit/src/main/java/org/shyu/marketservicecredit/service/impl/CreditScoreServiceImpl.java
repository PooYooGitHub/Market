package org.shyu.marketservicecredit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.shyu.marketcommon.exception.BusinessException;
import org.shyu.marketservicecredit.entity.CreditScore;
import org.shyu.marketapicredit.enums.CreditLevel;
import org.shyu.marketservicecredit.mapper.CreditScoreMapper;
import org.shyu.marketservicecredit.mapper.EvaluationMapper;
import org.shyu.marketservicecredit.service.CreditScoreService;
import org.shyu.marketapicredit.vo.CreditVO;

/**
 * 信用分服务实现类
 *
 * @author Market Team
 * @since 2026-04-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreditScoreServiceImpl extends ServiceImpl<CreditScoreMapper, CreditScore> implements CreditScoreService {

    private final EvaluationMapper evaluationMapper;

    @Override
    public CreditVO getUserCredit(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        // 获取信用分记录，如果没有则初始化
        CreditScore creditScore = baseMapper.getByUserId(userId);
        if (creditScore == null) {
            initUserCredit(userId);
            creditScore = baseMapper.getByUserId(userId);
        }

        // 构建返回对象
        CreditVO creditVO = new CreditVO();
        creditVO.setUserId(userId);
        creditVO.setScoreAndLevel(creditScore.getScore());

        // 统计评价信息
        Long totalEvaluations = evaluationMapper.getTotalCount(userId);
        Long goodCount = evaluationMapper.getGoodCount(userId);
        Double avgScore = evaluationMapper.getAvgScore(userId);

        creditVO.setTotalEvaluations(totalEvaluations);
        creditVO.setAvgScore(avgScore != null ? Math.round(avgScore * 100.0) / 100.0 : 0.0);

        // 计算好评率
        if (totalEvaluations > 0) {
            double goodRate = (goodCount.doubleValue() / totalEvaluations.doubleValue()) * 100;
            creditVO.setGoodRate(Math.round(goodRate * 100.0) / 100.0);
        } else {
            creditVO.setGoodRate(0.0);
        }

        return creditVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initUserCredit(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        try {
            baseMapper.initUserCredit(userId);
            log.info("初始化用户信用分成功，用户ID: {}", userId);
        } catch (Exception e) {
            log.error("初始化用户信用分失败，用户ID: {}", userId, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserCredit(Long userId, Integer scoreChange) {
        if (userId == null || scoreChange == null) {
            return;
        }

        CreditScore creditScore = baseMapper.getByUserId(userId);
        if (creditScore == null) {
            initUserCredit(userId);
            creditScore = baseMapper.getByUserId(userId);
        }

        // 计算新分数
        Integer newScore = creditScore.getScore() + scoreChange;

        // 限制分数范围在0-100之间
        newScore = Math.max(0, Math.min(100, newScore));

        // 更新信用分和等级
        creditScore.setScore(newScore);
        CreditLevel level = CreditLevel.getByScore(newScore);
        creditScore.setLevel(level.getName());

        updateById(creditScore);

        log.info("更新用户信用分成功，用户ID: {}, 分数变化: {}, 当前分数: {}, 等级: {}",
                userId, scoreChange, newScore, level.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recalculateUserCredit(Long userId) {
        if (userId == null) {
            return;
        }

        // 基础分数设为80分（中位数）
        int baseScore = 80;

        // 获取用户的评价统计
        Long totalCount = evaluationMapper.getTotalCount(userId);
        Double avgScore = evaluationMapper.getAvgScore(userId);

        if (totalCount > 0 && avgScore != null) {
            // 根据平均评分调整信用分，调整幅度更小
            // 5分: +20, 4分: +10, 3分: 0, 2分: -10, 1分: -20
            int scoreAdjustment = (int) ((avgScore - 3) * 10);

            // 根据评价数量给予额外加分，但更保守（最多+10分）
            int countBonus = Math.min(10, totalCount.intValue());

            baseScore += scoreAdjustment + countBonus;
        }

        // 更新信用分
        CreditScore creditScore = baseMapper.getByUserId(userId);
        if (creditScore == null) {
            initUserCredit(userId);
            creditScore = baseMapper.getByUserId(userId);
        }

        // 限制分数范围
        baseScore = Math.max(0, Math.min(100, baseScore));

        creditScore.setScore(baseScore);
        CreditLevel level = CreditLevel.getByScore(baseScore);
        creditScore.setLevel(level.getName());

        updateById(creditScore);

        log.info("重新计算用户信用分完成，用户ID: {}, 新分数: {}, 等级: {}", userId, baseScore, level.getName());
    }
}