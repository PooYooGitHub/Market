package org.shyu.marketservicearbitration.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 仲裁申请实体类
 * @author shyu
 * @since 2026-04-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_arbitration")
public class ArbitrationEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联订单ID
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 申请人ID
     */
    @TableField("applicant_id")
    private Long applicantId;

    /**
     * 被申诉人ID
     */
    @TableField("respondent_id")
    private Long respondentId;

    /**
     * 仲裁原因
     */
    @TableField("reason")
    private String reason;

    /**
     * 详细描述
     */
    @TableField("description")
    private String description;

    /**
     * 证据材料JSON
     */
    @TableField("evidence")
    private String evidence;

    @TableField("source_dispute_id")
    private Long sourceDisputeId;

    @TableField("request_type")
    private String requestType;

    @TableField("request_description")
    private String requestDescription;

    @TableField("expected_amount")
    private BigDecimal expectedAmount;

    @TableField("buyer_claim")
    private String buyerClaim;

    /**
     * 状态 0:待处理 1:处理中 2:已完结 3:已驳回
     */
    @TableField("status")
    private Integer status;

    /**
     * 裁决结果
     */
    @TableField("result")
    private String result;

    @TableField("decision_remark")
    private String decisionRemark;

    @TableField("reject_reason")
    private String rejectReason;

    /**
     * 处理管理员ID
     */
    @TableField("handler_id")
    private Long handlerId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
