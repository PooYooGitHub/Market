package org.shyu.marketservicetrade.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * 订单统计信息VO
 *
 * @author shyu
 * @since 2026-04-05
 */
@Data
public class OrderStatisticsVO {

    /**
     * 总订单数
     */
    private Long totalOrders;

    /**
     * 待付款订单数
     */
    private Long pendingPaymentOrders;

    /**
     * 待发货订单数
     */
    private Long pendingShipmentOrders;

    /**
     * 待收货订单数
     */
    private Long pendingReceiptOrders;

    /**
     * 已完成订单数
     */
    private Long completedOrders;

    /**
     * 已取消订单数
     */
    private Long cancelledOrders;

    /**
     * 今日新增订单数
     */
    private Long todayNewOrders;

    /**
     * 本周新增订单数
     */
    private Long weekNewOrders;

    /**
     * 本月新增订单数
     */
    private Long monthNewOrders;

    /**
     * 总交易金额
     */
    private BigDecimal totalAmount;

    /**
     * 今日交易金额
     */
    private BigDecimal todayAmount;

    /**
     * 本周交易金额
     */
    private BigDecimal weekAmount;

    /**
     * 本月交易金额
     */
    private BigDecimal monthAmount;

    /**
     * 平均订单金额
     */
    private BigDecimal averageOrderAmount;

    /**
     * 订单转化率
     */
    private Double orderConversionRate;

    /**
     * 统计时间
     */
    private LocalDateTime statisticsTime;
}