package org.shyu.marketservicetrade.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户余额VO
 */
@Data
@ApiModel("用户余额信息")
public class UserBalanceVO {

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("可用余额")
    private BigDecimal balance;

    @ApiModelProperty("冻结余额")
    private BigDecimal frozenBalance;

    @ApiModelProperty("总余额")
    private BigDecimal totalBalance;
}