package org.shyu.marketapiarbitration.dto;

import lombok.Data;

/**
 * 仲裁查询DTO
 *
 * @author Market Team
 * @since 2026-04-01
 */
@Data
public class ArbitrationQueryDTO {

    /**
     * 申请人ID
     */
    private Long applicantId;

    /**
     * 被申诉人ID
     */
    private Long respondentId;

    /**
     * 状态筛选
     */
    private Integer status;

    /**
     * 处理管理员ID
     */
    private Long handlerId;

    /**
     * 仲裁原因筛选
     */
    private String reason;

    /**
     * 关键字搜索（描述内容）
     */
    private String keyword;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField = "create_time";

    /**
     * 排序方向
     */
    private String sortOrder = "desc";
}