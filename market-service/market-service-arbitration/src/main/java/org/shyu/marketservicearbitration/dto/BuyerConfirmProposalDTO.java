package org.shyu.marketservicearbitration.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel("买家确认卖家方案DTO")
public class BuyerConfirmProposalDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "disputeId不能为空")
    @ApiModelProperty(value = "争议ID", required = true)
    private Long disputeId;

    @NotNull(message = "acceptProposal不能为空")
    @ApiModelProperty(value = "是否接受卖家方案", required = true)
    private Boolean acceptProposal;

    @ApiModelProperty("拒绝原因或补充说明")
    private String remark;
}

