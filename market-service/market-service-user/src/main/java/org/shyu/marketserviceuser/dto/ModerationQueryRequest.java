package org.shyu.marketserviceuser.dto;

import lombok.Data;

/**
 * 审核查询请求DTO
 */
@Data
public class ModerationQueryRequest {
    private String keyword;
    private Integer status;
    private String startDate;
    private String endDate;
    private Integer pageNum = 1;
    private Integer pageSize = 20;
}