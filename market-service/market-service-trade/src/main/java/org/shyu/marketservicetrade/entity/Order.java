package org.shyu.marketservicetrade.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体
 */
@Data
@TableName("t_order")
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 买家ID
     */
    private Long buyerId;

    /**
     * 卖家ID
     */
    private Long sellerId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 收货地址ID（下单时选择）
     */
    private Long addressId;

    /**
     * 收货人姓名（地址快照）
     */
    private String receiverName;

    /**
     * 收货人手机号（地址快照）
     */
    private String receiverPhone;

    /**
     * 收货省份（地址快照）
     */
    private String receiverProvince;

    /**
     * 收货城市（地址快照）
     */
    private String receiverCity;

    /**
     * 收货区县（地址快照）
     */
    private String receiverDistrict;

    /**
     * 收货详细地址（地址快照）
     */
    private String receiverDetailAddress;

    /**
     * 收货邮编（地址快照）
     */
    private String receiverPostalCode;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 订单状态
     * 0:待支付 1:已支付 2:已发货 3:已收货/完成 4:已取消 5:售后中
     */
    private Integer status;

    /**
     * 创建时间(下单时间)
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

