package org.shyu.marketservicetrade.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("tb_payment")
public class Payment {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 支付流水号（系统生成）
     */
    @TableField("payment_no")
    private String paymentNo;

    /**
     * 关联订单ID
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 订单号
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 用户ID（支付人）
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 支付金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 支付方式：1-支付宝，2-微信支付，3-余额支付，4-银行卡
     */
    @TableField("payment_method")
    private Integer paymentMethod;

    /**
     * 支付状态：0-待支付，1-支付成功，2-支付失败，3-已退款
     */
    @TableField("status")
    private Integer status;

    /**
     * 支付描述
     */
    @TableField("description")
    private String description;

    /**
     * 第三方支付流水号（模拟）
     */
    @TableField("third_party_no")
    private String thirdPartyNo;

    /**
     * 支付完成时间
     */
    @TableField("paid_time")
    private LocalDateTime paidTime;

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