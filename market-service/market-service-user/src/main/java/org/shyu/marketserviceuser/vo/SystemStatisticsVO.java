package org.shyu.marketserviceuser.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

/**
 * 系统统计数据VO
 *
 * @author shyu
 * @since 2026-04-05
 */
@Data
public class SystemStatisticsVO {

    /**
     * 用户增长趋势
     */
    private Map<String, Long> userGrowthTrend;

    /**
     * 商品发布趋势
     */
    private Map<String, Long> productPublishTrend;

    /**
     * 交易趋势
     */
    private Map<String, Long> tradeTrend;

    /**
     * 系统访问量趋势
     */
    private Map<String, Long> accessTrend;

    /**
     * 热门功能使用统计
     */
    private Map<String, Long> featureUsageStats;

    /**
     * 地区分布
     */
    private Map<String, Long> regionDistribution;

    /**
     * 设备类型分布
     */
    private Map<String, Long> deviceTypeDistribution;

    /**
     * 浏览器分布
     */
    private Map<String, Long> browserDistribution;

    /**
     * API调用统计
     */
    private Map<String, Long> apiCallStats;

    /**
     * 错误统计
     */
    private Map<String, Long> errorStats;

    /**
     * 性能指标
     */
    private PerformanceMetrics performanceMetrics;

    /**
     * 数据库统计
     */
    private DatabaseMetrics databaseMetrics;

    /**
     * 缓存统计
     */
    private CacheMetrics cacheMetrics;

    /**
     * 统计时间
     */
    private LocalDateTime statisticsTime;

    @Data
    public static class PerformanceMetrics {
        private Double averageResponseTime;
        private Double maxResponseTime;
        private Double systemLoad;
        private Long memoryUsage;
        private Long diskUsage;
        private Double cpuUsage;
    }

    @Data
    public static class DatabaseMetrics {
        private Long connectionCount;
        private Long queryCount;
        private Double averageQueryTime;
        private Long slowQueryCount;
        private Long tableSize;
    }

    @Data
    public static class CacheMetrics {
        private Long hitCount;
        private Long missCount;
        private Double hitRate;
        private Long evictionCount;
        private Long memoryUsage;
    }
}