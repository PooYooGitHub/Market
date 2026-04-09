package org.shyu.marketservicetrade.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * 创建支付请求
 */
@Data
public class CreatePaymentRequest {

    /**
     * 订单ID
     */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /**
     * 支付方式：1-支付宝，2-微信支付，3-余额支付，4-银行卡
     */
    @NotNull(message = "支付方式不能为空")
    private Integer paymentMethod;

    /**
     * 支付金额（用于校验）
     */
    @NotNull(message = "支付金额不能为空")
    @Positive(message = "支付金额必须大于0")
    private BigDecimal amount;

    /**
     * 支付密码（余额支付时需要）
     */
    private String payPassword;
}