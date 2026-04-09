package org.shyu.marketapicredit.dto;

import lombok.Data;

import javax.validation.constraints.*;

/**
 * 创建评价请求DTO
 *
 * @author Market Team
 * @since 2026-04-01
 */
@Data
public class CreateEvaluationDTO {

    /**
     * 订单ID
     */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /**
     * 被评价人ID
     */
    @NotNull(message = "被评价人ID不能为空")
    private Long targetId;

    /**
     * 评分 1-5
     */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分不能小于1")
    @Max(value = 5, message = "评分不能大于5")
    private Integer score;

    /**
     * 评价内容
     */
    @Size(max = 500, message = "评价内容不能超过500字")
    private String content;
}