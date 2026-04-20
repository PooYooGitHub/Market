package org.shyu.marketapicredit.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Credit level enum based on sesame-score style ranges.
 */
@Getter
@AllArgsConstructor
public enum CreditLevel {

    EXCELLENT("优秀", 850, 950, "#67C23A"),
    GOOD("良好", 750, 849, "#409EFF"),
    STABLE("稳定", 650, 749, "#E6A23C"),
    GROWING("成长中", 550, 649, "#909399"),
    NEW_USER("新手", 350, 549, "#C0C4CC");

    private final String name;
    private final Integer minScore;
    private final Integer maxScore;
    private final String color;

    public static CreditLevel getByScore(Integer score) {
        if (score == null) {
            return GROWING;
        }

        for (CreditLevel level : values()) {
            if (score >= level.minScore && score <= level.maxScore) {
                return level;
            }
        }

        if (score > EXCELLENT.maxScore) {
            return EXCELLENT;
        }
        return NEW_USER;
    }

    public static CreditLevel getByName(String name) {
        for (CreditLevel level : values()) {
            if (level.name.equals(name)) {
                return level;
            }
        }
        return GROWING;
    }
}
