package org.shyu.marketservicetrade.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单详情VO（管理员视图）
 * 包含更详细的信息，用于管理员查看
 */
@Data
public class OrderDetailVO implements Serializable {
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
     * 买家头像
     */
    private String buyerAvatar;

    /**
     * 买家手机号
     */
    private String buyerPhone;

    /**
     * 买家邮箱
     */
    private String buyerEmail;

    /**
     * 卖家ID
     */
    private Long sellerId;

    /**
     * 卖家昵称
     */
    private String sellerNickname;

    /**
     * 卖家头像
     */
    private String sellerAvatar;

    /**
     * 卖家手机号
     */
    private String sellerPhone;

    /**
     * 卖家邮箱
     */
    private String sellerEmail;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品标题
     */
    private String productTitle;

    /**
     * 商品描述
     */
    private String productDescription;

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
     * 优惠金额
     */
    private BigDecimal discountAmount;

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
     * 收货地址
     */
    private String deliveryAddress;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人电话
     */
    private String receiverPhone;

    /**
     * 快递单号
     */
    private String trackingNumber;

    /**
     * 买家留言
     */
    private String buyerMessage;

    /**
     * 卖家备注
     */
    private String sellerNote;

    /**
     * 管理员备注
     */
    private String adminNote;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 发货时间
     */
    private LocalDateTime shipTime;

    /**
     * 收货时间
     */
    private LocalDateTime receiveTime;

    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人IP
     */
    private String createIp;

    /**
     * 是否删除
     */
    private Boolean deleted;
}