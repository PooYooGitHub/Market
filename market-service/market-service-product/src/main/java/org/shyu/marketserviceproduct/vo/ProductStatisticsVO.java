package org.shyu.marketserviceproduct.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 商品统计信息VO
 *
 * @author shyu
 * @since 2026-04-05
 */
@Data
public class ProductStatisticsVO {

    /**
     * 总商品数
     */
    private Long totalProducts;

    /**
     * 待审核商品数
     */
    private Long pendingAuditProducts;

    /**
     * 已上架商品数
     */
    private Long onlineProducts;

    /**
     * 已下架商品数
     */
    private Long offlineProducts;

    /**
     * 今日新增商品数
     */
    private Long todayNewProducts;

    /**
     * 本周新增商品数
     */
    private Long weekNewProducts;

    /**
     * 本月新增商品数
     */
    private Long monthNewProducts;

    /**
     * 总浏览量
     */
    private Long totalViews;

    /**
     * 平均价格
     */
    private Double averagePrice;

    /**
     * 最高价格
     */
    private Double maxPrice;

    /**
     * 最低价格
     */
    private Double minPrice;

    /**
     * 统计时间
     */
    private LocalDateTime statisticsTime;
}