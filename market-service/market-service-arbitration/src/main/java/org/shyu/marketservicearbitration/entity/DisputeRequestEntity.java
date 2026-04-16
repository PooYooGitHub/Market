package org.shyu.marketservicearbitration.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_dispute_request")
public class DisputeRequestEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("order_id")
    private Long orderId;

    @TableField("product_id")
    private Long productId;

    @TableField("buyer_id")
    private Long buyerId;

    @TableField("seller_id")
    private Long sellerId;

    @TableField("reason")
    private String reason;

    @TableField("fact_description")
    private String factDescription;

    @TableField("request_type")
    private String requestType;

    @TableField("request_description")
    private String requestDescription;

    @TableField("expected_amount")
    private BigDecimal expectedAmount;

    @TableField("status")
    private String status;

    @TableField("current_round")
    private Integer currentRound;

    @TableField("seller_response_type")
    private String sellerResponseType;

    @TableField("seller_response_description")
    private String sellerResponseDescription;

    @TableField("seller_response_proposal_type")
    private String sellerResponseProposalType;

    @TableField("seller_response_amount")
    private BigDecimal sellerResponseAmount;

    @TableField("seller_response_freight_bearer")
    private String sellerResponseFreightBearer;

    @TableField("escalated_arbitration_id")
    private Long escalatedArbitrationId;

    @TableField("final_decision_type")
    private String finalDecisionType;

    @TableField("final_execution_status")
    private String finalExecutionStatus;

    @TableField("final_result_description")
    private String finalResultDescription;

    @TableField("expire_time")
    private LocalDateTime expireTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
