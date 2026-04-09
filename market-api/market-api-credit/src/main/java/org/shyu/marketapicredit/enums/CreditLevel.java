package org.shyu.marketapicredit.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 信用等级枚举
 *
 * @author Market Team
 * @since 2026-04-01
 */
@Getter
@AllArgsConstructor
public enum CreditLevel {

    EXCELLENT("优秀", 90, 100, "#67C23A"),
    GOOD("良好", 80, 89, "#409EFF"),
    AVERAGE("一般", 60, 79, "#E6A23C"),
    POOR("较差", 40, 59, "#F56C6C"),
    VERY_POOR("很差", 0, 39, "#909399");

    /**
     * 等级名称
     */
    private final String name;

    /**
     * 最小分数
     */
    private final Integer minScore;

    /**
     * 最大分数
     */
    private final Integer maxScore;

    /**
     * 显示颜色
     */
    private final String color;

    /**
     * 根据分数获取信用等级
     *
     * @param score 信用分
     * @return 信用等级
     */
    public static CreditLevel getByScore(Integer score) {
        if (score == null) {
            return AVERAGE;
        }

        for (CreditLevel level : values()) {
            if (score >= level.minScore && score <= level.maxScore) {
                return level;
            }
        }

        return AVERAGE;
    }

    /**
     * 根据等级名称获取信用等级
     *
     * @param name 等级名称
     * @return 信用等级
     */
    public static CreditLevel getByName(String name) {
        for (CreditLevel level : values()) {
            if (level.name.equals(name)) {
                return level;
            }
        }
        return AVERAGE;
    }
}