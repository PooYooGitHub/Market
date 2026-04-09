package org.shyu.marketserviceproduct.vo;

import lombok.Data;

/**
 * 平台统计数据VO
 */
@Data
public class PlatformStatisticsVO {

    /**
     * 注册用户数
     */
    private Long userCount;

    /**
     * 在售商品数
     */
    private Long productCount;

    /**
     * 成功交易数
     */
    private Long orderCount;
}