package org.shyu.marketservicecredit.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.shyu.marketapicredit.enums.CreditLevel;

/**
 * Credit system policies.
 */
public final class CreditPolicy {

    private CreditPolicy() {
    }

    public static final int MIN_SCORE = 350;
    public static final int MAX_SCORE = 950;
    public static final int INITIAL_SCORE = 550;

    public static final int TRADE_COMPLETE_SCORE = 4;
    public static final int HIGH_TRUST_THRESHOLD = 850;

    public static int normalizeScore(int score) {
        return Math.max(MIN_SCORE, Math.min(MAX_SCORE, score));
    }

    public static int mapEvaluationScoreChange(Integer score) {
        if (score == null) {
            return 0;
        }
        switch (score) {
            case 5:
                return 6;
            case 4:
                return 3;
            case 3:
                return 0;
            case 2:
                return -4;
            case 1:
                return -8;
            default:
                return 0;
        }
    }

    /**
     * Positive delta is decayed based on current score. Negative delta is untouched.
     */
    public static int calculateEffectiveChange(int currentScore, int rawChange) {
        if (rawChange <= 0) {
            return rawChange;
        }

        double factor = resolvePositiveFactor(currentScore);
        int effective = (int) Math.round(rawChange * factor);
        return Math.max(1, effective);
    }

    private static double resolvePositiveFactor(int currentScore) {
        if (currentScore > 850) {
            return 0.2D;
        }
        if (currentScore > 700) {
            return 0.5D;
        }
        return 1.0D;
    }

    public static boolean isHighTrust(int score) {
        return score >= HIGH_TRUST_THRESHOLD;
    }

    public static CreditLevel resolveLevel(int score) {
        return CreditLevel.getByScore(score);
    }

    public static BadgeProfile resolveBadge(int score) {
        if (score >= 900) {
            return new BadgeProfile("DIAMOND", "钻石信誉", "#67C23A", "长期稳定高信用用户");
        }
        if (score >= 850) {
            return new BadgeProfile("GOLD", "金牌信誉", "#E6A23C", "高可信交易伙伴");
        }
        if (score >= 750) {
            return new BadgeProfile("SILVER", "银牌信誉", "#909399", "信用良好，可放心交易");
        }
        if (score >= 650) {
            return new BadgeProfile("BRONZE", "铜牌信誉", "#B88230", "信用稳定，持续成长中");
        }
        return new BadgeProfile("ROOKIE", "新手认证", "#C0C4CC", "交易次数较少，建议先小额交易");
    }

    @Getter
    @AllArgsConstructor
    public static class BadgeProfile {
        private final String code;
        private final String name;
        private final String color;
        private final String desc;
    }
}
