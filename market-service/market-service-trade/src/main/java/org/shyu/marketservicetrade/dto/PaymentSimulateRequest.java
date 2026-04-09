package org.shyu.marketservicetrade.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 支付模拟请求
 */
@Data
@ApiModel("支付模拟请求")
public class PaymentSimulateRequest {

    @NotBlank(message = "支付结果不能为空")
    @ApiModelProperty("支付结果：success-成功，failed-失败，cancel-取消")
    private String result;

    @ApiModelProperty("失败原因（当result为failed时需要）")
    private String failReason;
}