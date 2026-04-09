package org.shyu.marketservicearbitration.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 仲裁申请展示对象
 * @author shyu
 * @since 2026-04-01
 */
@Data
@ApiModel("仲裁申请展示对象")
public class ArbitrationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("仲裁ID")
    private Long id;

    @ApiModelProperty("订单ID")
    private Long orderId;

    @ApiModelProperty("申请人ID")
    private Long applicantId;

    @ApiModelProperty("申请人姓名")
    private String applicantName;

    @ApiModelProperty("被申诉人ID")
    private Long respondentId;

    @ApiModelProperty("被申诉人姓名")
    private String respondentName;

    @ApiModelProperty("仲裁原因")
    private String reason;

    @ApiModelProperty("仲裁原因描述")
    private String reasonDesc;

    @ApiModelProperty("详细描述")
    private String description;

    @ApiModelProperty("证据材料")
    private String evidence;

    @ApiModelProperty("状态: 0-待处理 1-处理中 2-已完结 3-已驳回")
    private Integer status;

    @ApiModelProperty("状态描述")
    private String statusDesc;

    @ApiModelProperty("裁决结果")
    private String result;

    @ApiModelProperty("处理管理员ID")
    private Long handlerId;

    @ApiModelProperty("处理管理员姓名")
    private String handlerName;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

}