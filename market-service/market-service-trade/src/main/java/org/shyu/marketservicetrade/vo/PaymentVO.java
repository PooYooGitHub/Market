package org.shyu.marketservicetrade.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付信息VO
 */
@Data
public class PaymentVO {

    /**
     * 支付ID
     */
    private Long id;

    /**
     * 支付流水号
     */
    private String paymentNo;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付方式代码
     */
    private Integer paymentMethod;

    /**
     * 支付方式描述
     */
    private String paymentMethodDesc;

    /**
     * 支付状态代码
     */
    private Integer status;

    /**
     * 支付状态描述
     */
    private String statusDesc;

    /**
     * 支付描述
     */
    private String description;

    /**
     * 第三方支付流水号
     */
    private String thirdPartyNo;

    /**
     * 支付完成时间
     */
    private LocalDateTime paidTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    // 订单相关信息
    /**
     * 商品标题
     */
    private String productTitle;

    /**
     * 商品图片
     */
    private String productImage;

    /**
     * 卖家昵称
     */
    private String sellerNickname;
}