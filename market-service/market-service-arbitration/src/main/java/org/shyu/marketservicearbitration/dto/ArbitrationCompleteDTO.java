package org.shyu.marketservicearbitration.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ApiModel("管理员完结仲裁DTO")
public class ArbitrationCompleteDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "arbitrationId不能为空")
    @ApiModelProperty(value = "仲裁ID", required = true)
    private Long arbitrationId;

    @NotBlank(message = "decisionRemark不能为空")
    @ApiModelProperty(value = "裁决备注", required = true)
    private String decisionRemark;

    @ApiModelProperty(value = "裁决类型：SUPPORT_FULL_REFUND / SUPPORT_PARTIAL_REFUND / SUPPORT_RETURN_AND_REFUND / SUPPORT_REPLACE / REJECT_BUYER_REQUEST / REQUIRE_SUPPLEMENT / CLOSE_WITH_NEGOTIATION_RESULT / OTHER")
    private String decisionType;

    @ApiModelProperty(value = "执行备注")
    private String executionRemark;

    @ApiModelProperty(value = "执行载荷JSON")
    private String executionPayload;

    @ApiModelProperty(value = "裁决金额（部分退款等场景可选）")
    private BigDecimal decidedAmount;
}

