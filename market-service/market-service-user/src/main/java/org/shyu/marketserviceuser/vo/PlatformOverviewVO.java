package org.shyu.marketserviceuser.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * 平台总览数据VO
 *
 * @author shyu
 * @since 2026-04-05
 */
@Data
public class PlatformOverviewVO {

    /**
     * 用户相关统计
     */
    private UserOverview userOverview;

    /**
     * 商品相关统计
     */
    private ProductOverview productOverview;

    /**
     * 交易相关统计
     */
    private TradeOverview tradeOverview;

    /**
     * 系统相关统计
     */
    private SystemOverview systemOverview;

    /**
     * 统计时间
     */
    private LocalDateTime statisticsTime;

    @Data
    public static class UserOverview {
        private Long totalUsers;
        private Long activeUsers;
        private Long newUsersToday;
        private Long onlineUsers;
    }

    @Data
    public static class ProductOverview {
        private Long totalProducts;
        private Long onlineProducts;
        private Long pendingAuditProducts;
        private Long newProductsToday;
    }

    @Data
    public static class TradeOverview {
        private Long totalOrders;
        private BigDecimal totalAmount;
        private Long ordersToday;
        private BigDecimal amountToday;
        private Double orderConversionRate;
    }

    @Data
    public static class SystemOverview {
        private String systemStatus;
        private Integer serviceCount;
        private Integer healthyServices;
        private Double systemLoad;
        private Long memoryUsage;
        private Long diskUsage;
    }
}