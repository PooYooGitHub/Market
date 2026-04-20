package org.shyu.marketservicecredit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.shyu.marketapicredit.enums.CreditLevel;
import org.shyu.marketapicredit.vo.CreditVO;
import org.shyu.marketcommon.exception.BusinessException;
import org.shyu.marketservicecredit.constant.CreditPolicy;
import org.shyu.marketservicecredit.entity.CreditLog;
import org.shyu.marketservicecredit.entity.CreditScore;
import org.shyu.marketservicecredit.enums.CreditEventType;
import org.shyu.marketservicecredit.mapper.CreditLogMapper;
import org.shyu.marketservicecredit.mapper.CreditScoreMapper;
import org.shyu.marketservicecredit.mapper.EvaluationMapper;
import org.shyu.marketservicecredit.service.CreditScoreService;

/**
 * Credit score service implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreditScoreServiceImpl extends ServiceImpl<CreditScoreMapper, CreditScore> implements CreditScoreService {

    private final EvaluationMapper evaluationMapper;
    private final CreditLogMapper creditLogMapper;

    @Override
    public CreditVO getUserCredit(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        CreditScore creditScore = baseMapper.getByUserId(userId);
        if (creditScore == null) {
            initUserCredit(userId);
            creditScore = baseMapper.getByUserId(userId);
        }

        if (creditScore == null) {
            throw new BusinessException("信用信息初始化失败");
        }

        boolean needRefresh = ensureProfileData(creditScore);
        if (needRefresh) {
            updateById(creditScore);
        }

        CreditVO creditVO = new CreditVO();
        creditVO.setUserId(userId);
        creditVO.setScoreAndLevel(creditScore.getScore());
        creditVO.setBadgeCode(creditScore.getBadgeCode());
        creditVO.setBadgeName(creditScore.getBadgeName());
        creditVO.setBadgeColor(creditScore.getBadgeColor());
        creditVO.setBadgeDesc(creditScore.getBadgeDesc());
        creditVO.setHighTrust(creditScore.getHighTrust() != null && creditScore.getHighTrust() == 1);
        creditVO.setValidTradeCount(safeInt(creditScore.getValidTradeCount()));

        Long totalEvaluations = evaluationMapper.getTotalCount(userId);
        Long goodCount = evaluationMapper.getGoodCount(userId);
        Double avgScore = evaluationMapper.getAvgScore(userId);

        creditVO.setTotalEvaluations(totalEvaluations == null ? 0L : totalEvaluations);
        creditVO.setAvgScore(avgScore != null ? round2(avgScore) : 0.0D);

        if (totalEvaluations != null && totalEvaluations > 0) {
            double goodRate = (goodCount.doubleValue() / totalEvaluations.doubleValue()) * 100D;
            creditVO.setGoodRate(round2(goodRate));
        } else {
            creditVO.setGoodRate(0.0D);
        }

        return creditVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initUserCredit(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        baseMapper.initUserCredit(userId);
        log.info("init credit score, userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserCredit(Long userId, Integer scoreChange) {
        applyCreditEvent(userId,
                CreditEventType.MANUAL_ADJUST,
                scoreChange,
                null,
                "manual credit update",
                null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recalculateUserCredit(Long userId) {
        if (userId == null) {
            return;
        }

        CreditScore creditScore = lockAndLoadCredit(userId);
        int validTradeCount = safeInt(creditScore.getValidTradeCount());

        int recalculated = CreditPolicy.INITIAL_SCORE;
        recalculated += Math.min(120, validTradeCount * 3);

        Long totalCount = evaluationMapper.getTotalCount(userId);
        Double avgScore = evaluationMapper.getAvgScore(userId);
        if (totalCount != null && totalCount > 0 && avgScore != null) {
            recalculated += (int) Math.round((avgScore - 3D) * 20D);
            recalculated += Math.min(40, totalCount.intValue());
        }

        recalculated = CreditPolicy.normalizeScore(recalculated);
        refreshCreditProfile(creditScore, recalculated);

        updateById(creditScore);
        log.info("recalculate user credit, userId={}, score={}", userId, recalculated);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyCreditEvent(Long userId,
                                 CreditEventType eventType,
                                 Integer rawScoreChange,
                                 Long relatedId,
                                 String reason,
                                 String eventKey) {
        if (userId == null || eventType == null) {
            throw new BusinessException("信用事件参数不完整");
        }

        CreditScore creditScore = lockAndLoadCredit(userId);

        if (StringUtils.hasText(eventKey) && creditLogMapper.countByEventKey(eventKey) > 0) {
            log.info("skip duplicated credit event, userId={}, eventKey={}", userId, eventKey);
            return;
        }

        int rawDelta = rawScoreChange == null ? 0 : rawScoreChange;
        int beforeScore = CreditPolicy.normalizeScore(safeScore(creditScore.getScore()));
        int effectiveDelta = CreditPolicy.calculateEffectiveChange(beforeScore, rawDelta);
        int afterScore = CreditPolicy.normalizeScore(beforeScore + effectiveDelta);

        refreshCreditProfile(creditScore, afterScore);

        if (CreditEventType.TRADE_COMPLETED == eventType && rawDelta > 0) {
            creditScore.setValidTradeCount(safeInt(creditScore.getValidTradeCount()) + 1);
        }

        updateById(creditScore);

        CreditLog creditLog = new CreditLog();
        creditLog.setUserId(userId);
        creditLog.setChangeType(eventType.getCode());
        creditLog.setRawScoreChange(rawDelta);
        creditLog.setScoreChange(effectiveDelta);
        creditLog.setBeforeScore(beforeScore);
        creditLog.setAfterScore(afterScore);
        creditLog.setRelatedId(relatedId);
        creditLog.setReason(StringUtils.hasText(reason) ? reason : eventType.getDescription());
        creditLog.setEventKey(StringUtils.hasText(eventKey) ? eventKey : null);
        creditLogMapper.insert(creditLog);

        log.info("credit event applied, userId={}, event={}, rawDelta={}, effectiveDelta={}, before={}, after={}, eventKey={}",
                userId, eventType.getCode(), rawDelta, effectiveDelta, beforeScore, afterScore, eventKey);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleTradeCompleted(Long orderId, Long buyerId, Long sellerId) {
        if (orderId == null || buyerId == null || sellerId == null) {
            throw new BusinessException("交易完成事件参数不完整");
        }

        applyCreditEvent(buyerId,
                CreditEventType.TRADE_COMPLETED,
                CreditPolicy.TRADE_COMPLETE_SCORE,
                orderId,
                "valid trade completed",
                buildEventKey(CreditEventType.TRADE_COMPLETED, orderId, buyerId));

        applyCreditEvent(sellerId,
                CreditEventType.TRADE_COMPLETED,
                CreditPolicy.TRADE_COMPLETE_SCORE,
                orderId,
                "valid trade completed",
                buildEventKey(CreditEventType.TRADE_COMPLETED, orderId, sellerId));
    }

    private CreditScore lockAndLoadCredit(Long userId) {
        CreditScore creditScore = baseMapper.getByUserIdForUpdate(userId);
        if (creditScore == null) {
            baseMapper.initUserCredit(userId);
            creditScore = baseMapper.getByUserIdForUpdate(userId);
        }

        if (creditScore == null) {
            throw new BusinessException("信用记录初始化失败");
        }

        ensureProfileData(creditScore);
        return creditScore;
    }

    private boolean ensureProfileData(CreditScore creditScore) {
        boolean needRefresh = false;

        if (creditScore.getScore() == null) {
            creditScore.setScore(CreditPolicy.INITIAL_SCORE);
            needRefresh = true;
        }

        if (creditScore.getValidTradeCount() == null) {
            creditScore.setValidTradeCount(0);
            needRefresh = true;
        }

        if (!StringUtils.hasText(creditScore.getLevel())
                || !StringUtils.hasText(creditScore.getBadgeCode())
                || !StringUtils.hasText(creditScore.getBadgeName())
                || !StringUtils.hasText(creditScore.getBadgeColor())
                || !StringUtils.hasText(creditScore.getBadgeDesc())
                || creditScore.getHighTrust() == null) {
            refreshCreditProfile(creditScore, safeScore(creditScore.getScore()));
            needRefresh = true;
        }

        return needRefresh;
    }

    private void refreshCreditProfile(CreditScore creditScore, int score) {
        int normalized = CreditPolicy.normalizeScore(score);
        CreditLevel level = CreditPolicy.resolveLevel(normalized);
        CreditPolicy.BadgeProfile badgeProfile = CreditPolicy.resolveBadge(normalized);

        creditScore.setScore(normalized);
        creditScore.setLevel(level.getName());
        creditScore.setBadgeCode(badgeProfile.getCode());
        creditScore.setBadgeName(badgeProfile.getName());
        creditScore.setBadgeColor(badgeProfile.getColor());
        creditScore.setBadgeDesc(badgeProfile.getDesc());
        creditScore.setHighTrust(CreditPolicy.isHighTrust(normalized) ? 1 : 0);

        if (creditScore.getValidTradeCount() == null) {
            creditScore.setValidTradeCount(0);
        }
    }

    private String buildEventKey(CreditEventType eventType, Long relatedId, Long userId) {
        return eventType.getCode() + ":" + relatedId + ":" + userId;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private int safeScore(Integer score) {
        return score == null ? CreditPolicy.INITIAL_SCORE : score;
    }

    private double round2(double value) {
        return Math.round(value * 100.0D) / 100.0D;
    }
}
