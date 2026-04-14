package org.shyu.marketservicearbitration.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("买家发起争议DTO")
public class DisputeCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "orderId不能为空")
    @ApiModelProperty(value = "订单ID", required = true)
    private Long orderId;

    @ApiModelProperty("商品ID，可不传")
    private Long productId;

    @NotBlank(message = "reason不能为空")
    @ApiModelProperty(value = "争议原因", required = true)
    private String reason;

    @NotBlank(message = "factDescription不能为空")
    @ApiModelProperty(value = "事实说明", required = true)
    private String factDescription;

    @NotBlank(message = "requestType不能为空")
    @ApiModelProperty(value = "诉求类型", required = true)
    private String requestType;

    @NotBlank(message = "requestDescription不能为空")
    @ApiModelProperty(value = "诉求说明", required = true)
    private String requestDescription;

    @DecimalMin(value = "0.00", message = "expectedAmount不能小于0")
    @ApiModelProperty("期望金额")
    private BigDecimal expectedAmount;

    @Valid
    @ApiModelProperty("证据列表")
    private List<EvidenceItemDTO> evidenceList = new ArrayList<>();

    @Data
    public static class EvidenceItemDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty("证据类型")
        private String evidenceType;

        @ApiModelProperty("标题")
        private String title;

        @ApiModelProperty("说明")
        private String description;

        @NotBlank(message = "fileUrl不能为空")
        @ApiModelProperty(value = "文件URL", required = true)
        private String fileUrl;

        @ApiModelProperty("缩略图URL")
        private String thumbnailUrl;
    }
}

