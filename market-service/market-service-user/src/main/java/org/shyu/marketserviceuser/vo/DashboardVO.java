package org.shyu.marketserviceuser.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

/**
 * 管理员仪表盘数据VO
 *
 * @author shyu
 * @since 2026-04-05
 */
@Data
public class DashboardVO {

    /**
     * 关键指标卡片数据
     */
    private KeyMetrics keyMetrics;

    /**
     * 趋势图数据
     */
    private TrendCharts trendCharts;

    /**
     * 排行榜数据
     */
    private Rankings rankings;

    /**
     * 实时数据
     */
    private RealtimeData realtimeData;

    /**
     * 快捷操作
     */
    private List<QuickAction> quickActions;

    /**
     * 系统提醒
     */
    private List<SystemAlert> systemAlerts;

    /**
     * 数据更新时间
     */
    private LocalDateTime updateTime;

    @Data
    public static class KeyMetrics {
        private Long totalUsers;
        private Long totalProducts;
        private Long totalOrders;
        private BigDecimal totalAmount;
        private Long userGrowthRate; // 用户增长率百分比
        private Long productGrowthRate; // 商品增长率百分比
        private Long orderGrowthRate; // 订单增长率百分比
        private Long amountGrowthRate; // 交易额增长率百分比
    }

    @Data
    public static class TrendCharts {
        // 用户注册趋势 (最近30天)
        private Map<String, Long> userRegistrationTrend;
        // 商品发布趋势 (最近30天)
        private Map<String, Long> productPublishTrend;
        // 交易趋势 (最近30天)
        private Map<String, Long> orderTrend;
        // 收入趋势 (最近30天)
        private Map<String, BigDecimal> revenueTrend;
        // 活跃用户趋势 (最近30天)
        private Map<String, Long> activeUserTrend;
    }

    @Data
    public static class Rankings {
        // 热门商品排行
        private List<RankingItem> hotProducts;
        // 活跃用户排行
        private List<RankingItem> activeUsers;
        // 热门分类排行
        private List<RankingItem> hotCategories;
        // 销售排行
        private List<RankingItem> topSellers;
    }

    @Data
    public static class RankingItem {
        private String name;
        private Long value;
        private String description;
        private String changeRate; // 变化率
    }

    @Data
    public static class RealtimeData {
        private Long onlineUsers; // 在线用户数
        private Long todayVisits; // 今日访问量
        private Long todayOrders; // 今日订单数
        private BigDecimal todayAmount; // 今日交易额
        private Long pendingOrders; // 待处理订单
        private Long pendingAuditProducts; // 待审核商品
    }

    @Data
    public static class QuickAction {
        private String name;
        private String description;
        private String icon;
        private String url;
        private String type; // link, modal, action
    }

    @Data
    public static class SystemAlert {
        private String type; // info, warning, error, success
        private String title;
        private String message;
        private LocalDateTime time;
        private Boolean read;
    }
}