package org.shyu.marketservicearbitration.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel("升级仲裁DTO")
public class DisputeEscalateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "disputeId不能为空")
    @ApiModelProperty(value = "争议ID", required = true)
    private Long disputeId;

    @ApiModelProperty("升级原因")
    private String escalateReason;
}

