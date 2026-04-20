package org.shyu.marketapicredit.vo;

import lombok.Data;
import org.shyu.marketapicredit.enums.CreditLevel;

/**
 * Credit info response.
 */
@Data
public class CreditVO {

    private Long userId;
    private Integer score;
    private String level;
    private String levelColor;

    private String badgeCode;
    private String badgeName;
    private String badgeColor;
    private String badgeDesc;
    private Boolean highTrust;
    private Integer validTradeCount;

    private Long totalEvaluations;
    private Double goodRate;
    private Double avgScore;

    public void setScoreAndLevel(Integer score) {
        this.score = score;
        CreditLevel creditLevel = CreditLevel.getByScore(score);
        this.level = creditLevel.getName();
        this.levelColor = creditLevel.getColor();
    }
}
