package org.shyu.marketservicetrade.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 订单退款请求DTO
 *
 * @author shyu
 * @since 2026-04-05
 */
@Data
public class OrderRefundRequest {

    /**
     * 退款类型：1-同意退款，2-拒绝退款
     */
    @NotNull(message = "退款类型不能为空")
    private Integer refundType;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 处理说明
     */
    private String processRemark;

    /**
     * 退款到账方式：1-原路退回，2-退到余额
     */
    private Integer refundMethod;
}