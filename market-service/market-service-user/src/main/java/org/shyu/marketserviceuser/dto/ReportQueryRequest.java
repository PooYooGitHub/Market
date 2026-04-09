package org.shyu.marketserviceuser.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.util.Map;

/**
 * 报表查询请求DTO
 *
 * @author shyu
 * @since 2026-04-05
 */
@Data
public class ReportQueryRequest {

    /**
     * 报表类型
     */
    @NotBlank(message = "报表类型不能为空")
    private String reportType;

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;

    /**
     * 时间维度：day, week, month, year
     */
    private String timeDimension = "day";

    /**
     * 分页参数
     */
    private Integer pageNum = 1;
    private Integer pageSize = 20;

    /**
     * 排序字段
     */
    private String sortBy;

    /**
     * 排序方向：asc, desc
     */
    private String sortOrder = "desc";

    /**
     * 筛选条件
     */
    private Map<String, Object> filters;

    /**
     * 分组条件
     */
    private String groupBy;

    /**
     * 聚合函数：sum, count, avg, max, min
     */
    private String aggregateFunction = "count";

    /**
     * 是否包含详细数据
     */
    private Boolean includeDetails = false;

    /**
     * 导出格式：excel, pdf, csv
     */
    private String exportFormat;
}