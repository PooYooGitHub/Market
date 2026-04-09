package org.shyu.marketapicredit.vo;

import lombok.Data;
import org.shyu.marketapicredit.enums.CreditLevel;

/**
 * 信用信息响应VO
 *
 * @author Market Team
 * @since 2026-04-01
 */
@Data
public class CreditVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 信用分
     */
    private Integer score;

    /**
     * 信用等级
     */
    private String level;

    /**
     * 等级颜色
     */
    private String levelColor;

    /**
     * 评价总数
     */
    private Long totalEvaluations;

    /**
     * 好评率
     */
    private Double goodRate;

    /**
     * 平均评分
     */
    private Double avgScore;

    /**
     * 根据分数设置等级信息
     */
    public void setScoreAndLevel(Integer score) {
        this.score = score;
        CreditLevel creditLevel = CreditLevel.getByScore(score);
        this.level = creditLevel.getName();
        this.levelColor = creditLevel.getColor();
    }
}