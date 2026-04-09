package org.shyu.marketservicetrade.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 支付查询参数
 */
@Data
@ApiModel("支付查询参数")
public class PaymentQueryDTO {

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("支付状态：pending-支付中，success-支付成功，failed-支付失败，cancelled-已取消")
    private String status;

    @ApiModelProperty("支付方式：alipay-支付宝，wechat-微信，balance-余额，bank-银行卡")
    private String paymentMethod;

    @ApiModelProperty("开始时间")
    private String startTime;

    @ApiModelProperty("结束时间")
    private String endTime;
}