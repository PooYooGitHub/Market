package org.shyu.marketservicearbitration.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ApiModel("卖家响应争议DTO")
public class SellerDisputeResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "disputeId不能为空")
    @ApiModelProperty(value = "争议ID", required = true)
    private Long disputeId;

    @NotBlank(message = "responseType不能为空")
    @ApiModelProperty(value = "响应类型: AGREE/REJECT/PROPOSE", required = true)
    private String responseType;

    @ApiModelProperty("响应说明")
    private String responseDescription;

    @ApiModelProperty("替代方案类型")
    private String proposalType;

    @DecimalMin(value = "0.00", message = "proposalAmount不能小于0")
    @ApiModelProperty("替代方案金额")
    private BigDecimal proposalAmount;

    @ApiModelProperty("替代方案说明")
    private String proposalDescription;

    @ApiModelProperty("运费承担方")
    private String freightBearer;
}

