package org.shyu.marketservicetrade.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单列表VO（管理员视图）
 * 用于分页列表显示
 */
@Data
public class OrderListVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
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
     * 买家昵称
     */
    private String buyerNickname;

    /**
     * 卖家ID
     */
    private Long sellerId;

    /**
     * 卖家昵称
     */
    private String sellerNickname;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品标题
     */
    private String productTitle;

    /**
     * 商品图片
     */
    private String productImage;

    /**
     * 商品价格
     */
    private BigDecimal productPrice;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 实付金额
     */
    private BigDecimal actualAmount;

    /**
     * 订单状态
     * 0:待支付 1:已支付 2:已发货 3:已收货/完成 4:已取消 5:售后中
     */
    private Integer status;

    /**
     * 订单状态描述
     */
    private String statusDesc;

    /**
     * 支付方式
     * 1:余额支付 2:第三方支付
     */
    private Integer paymentMethod;

    /**
     * 支付方式描述
     */
    private String paymentMethodDesc;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否有售后
     */
    private Boolean hasAfterSale;

    /**
     * 是否有争议
     */
    private Boolean hasDispute;

    /**
     * 买家评价状态
     * 0:未评价 1:已评价
     */
    private Integer buyerReviewStatus;

    /**
     * 卖家评价状态
     * 0:未评价 1:已评价
     */
    private Integer sellerReviewStatus;
}