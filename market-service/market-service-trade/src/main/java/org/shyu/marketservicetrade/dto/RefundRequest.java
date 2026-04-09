package org.shyu.marketservicetrade.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 退款申请请求
 */
@Data
@ApiModel("退款申请请求")
public class RefundRequest {

    @NotNull(message = "订单ID不能为空")
    @ApiModelProperty("订单ID")
    private Long orderId;

    @NotBlank(message = "退款理由不能为空")
    @ApiModelProperty("退款理由")
    private String reason;

    @ApiModelProperty("退款金额（可选，默认全额退款）")
    private java.math.BigDecimal amount;
}