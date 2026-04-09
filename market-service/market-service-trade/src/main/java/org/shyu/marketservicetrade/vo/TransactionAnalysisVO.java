package org.shyu.marketservicetrade.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

/**
 * 交易分析数据VO
 *
 * @author shyu
 * @since 2026-04-05
 */
@Data
public class TransactionAnalysisVO {

    /**
     * 时间维度统计数据
     * key: 时间（格式：yyyy-MM-dd 或 yyyy-MM-dd HH:00:00）
     * value: 统计数据
     */
    private Map<String, TransactionDataVO> timeSeriesData;

    /**
     * 分类统计数据
     */
    private Map<String, TransactionDataVO> categoryData;

    /**
     * 地区统计数据
     */
    private Map<String, TransactionDataVO> regionData;

    /**
     * 支付方式统计
     */
    private Map<String, Long> paymentMethodStats;

    /**
     * 订单来源统计
     */
    private Map<String, Long> orderSourceStats;

    @Data
    public static class TransactionDataVO {
        /**
         * 订单数量
         */
        private Long orderCount;

        /**
         * 交易金额
         */
        private BigDecimal transactionAmount;

        /**
         * 用户数量
         */
        private Long userCount;

        /**
         * 商品数量
         */
        private Long productCount;
    }
}