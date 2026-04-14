package org.shyu.marketservicearbitration.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel("管理员驳回仲裁DTO")
public class ArbitrationRejectDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "arbitrationId不能为空")
    @ApiModelProperty(value = "仲裁ID", required = true)
    private Long arbitrationId;

    @NotBlank(message = "rejectReason不能为空")
    @ApiModelProperty(value = "驳回原因", required = true)
    private String rejectReason;
}
