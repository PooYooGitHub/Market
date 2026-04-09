package org.shyu.marketservicetrade.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 订单查询请求DTO
 */
@Data
public class OrderQueryRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 订单状态
     */
    private Integer status;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 10;
}

