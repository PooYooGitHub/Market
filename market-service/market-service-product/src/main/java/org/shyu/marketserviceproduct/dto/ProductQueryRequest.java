package org.shyu.marketserviceproduct.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 商品查询请求DTO
 */
@Data
public class ProductQueryRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 关键词 (标题、描述)
     */
    private String keyword;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 最小价格
     */
    private java.math.BigDecimal minPrice;

    /**
     * 最大价格
     */
    private java.math.BigDecimal maxPrice;

    /**
     * 排序字段 (price, view_count, create_time)
     */
    private String sortField = "create_time";

    /**
     * 排序方式 (asc, desc)
     */
    private String sortOrder = "desc";

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 20;

    /**
     * 商品状态 (用于管理员查询)
     */
    private Integer status;

    /**
     * 卖家ID (用于管理员查询)
     */
    private Long sellerId;
}

