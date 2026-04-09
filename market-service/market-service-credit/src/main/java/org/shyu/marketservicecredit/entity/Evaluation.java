package org.shyu.marketservicecredit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 评价实体类
 *
 * @author Market Team
 * @since 2026-04-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_evaluation")
public class Evaluation {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 评价人ID
     */
    private Long evaluatorId;

    /**
     * 被评价人ID
     */
    private Long targetId;

    /**
     * 评分 1-5
     */
    private Integer score;

    /**
     * 评价内容
     */
    @TableField("content")
    private String comment;

    /**
     * 评价时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}