package org.shyu.marketapicredit.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价信息响应VO
 *
 * @author Market Team
 * @since 2026-04-01
 */
@Data
public class EvaluationVO {

    /**
     * 评价ID
     */
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 评价人ID
     */
    private Long evaluatorId;

    /**
     * 评价人昵称
     */
    private String evaluatorName;

    /**
     * 评价人头像
     */
    private String evaluatorAvatar;

    /**
     * 被评价人ID
     */
    private Long targetId;

    /**
     * 被评价人昵称
     */
    private String targetName;

    /**
     * 被评价人头像
     */
    private String targetAvatar;

    /**
     * 评分 (1-5分)
     */
    private Integer score;

    /**
     * 评价内容
     */
    private String comment;

    /**
     * 商品标题
     */
    private String productTitle;

    /**
     * 商品图片
     */
    private String productImage;

    /**
     * 评价时间
     */
    private LocalDateTime createTime;

    /**
     * 评分描述
     */
    public String getScoreDesc() {
        if (score == null) {
            return "未评分";
        }
        switch (score) {
            case 1:
                return "非常不满意";
            case 2:
                return "不满意";
            case 3:
                return "一般";
            case 4:
                return "满意";
            case 5:
                return "非常满意";
            default:
                return "未知";
        }
    }

    /**
     * 是否为好评（4分及以上）
     */
    public Boolean isGoodReview() {
        return score != null && score >= 4;
    }
}