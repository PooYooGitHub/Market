package org.shyu.marketapiarbitration.vo;

import lombok.Data;
import org.shyu.marketapiarbitration.enums.ArbitrationStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 仲裁信息VO
 *
 * @author Market Team
 * @since 2026-04-01
 */
@Data
public class ArbitrationVO {

    /**
     * 仲裁ID
     */
    private Long id;

    /**
     * 关联订单ID
     */
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 申请人ID
     */
    private Long applicantId;

    /**
     * 申请人昵称
     */
    private String applicantName;

    /**
     * 申请人头像
     */
    private String applicantAvatar;

    /**
     * 被申诉人ID
     */
    private Long respondentId;

    /**
     * 被申诉人昵称
     */
    private String respondentName;

    /**
     * 被申诉人头像
     */
    private String respondentAvatar;

    /**
     * 仲裁原因
     */
    private String reason;

    /**
     * 仲裁原因描述
     */
    private String reasonDesc;

    /**
     * 详细描述
     */
    private String description;

    /**
     * 证据材料URL列表
     */
    private List<String> evidenceUrls;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 状态颜色
     */
    private String statusColor;

    /**
     * 裁决结果
     */
    private String result;

    /**
     * 处理管理员ID
     */
    private Long handlerId;

    /**
     * 处理管理员名称
     */
    private String handlerName;

    /**
     * 商品标题
     */
    private String productTitle;

    /**
     * 商品图片
     */
    private String productImage;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 设置状态并自动填充相关信息
     */
    public void setStatusAndDesc(Integer status) {
        this.status = status;
        ArbitrationStatus statusEnum = ArbitrationStatus.getByCode(status);
        this.statusDesc = statusEnum.getName();
        this.statusColor = statusEnum.getColor();
    }

    /**
     * 是否可以处理
     */
    public Boolean getCanHandle() {
        return ArbitrationStatus.getByCode(status).canHandle();
    }

    /**
     * 是否已结束
     */
    public Boolean getIsFinished() {
        return ArbitrationStatus.getByCode(status).isFinished();
    }
}