package org.shyu.marketserviceuser.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

/**
 * 报表数据VO
 *
 * @author shyu
 * @since 2026-04-05
 */
@Data
public class ReportVO {

    /**
     * 报表标题
     */
    private String title;

    /**
     * 报表类型
     */
    private String type;

    /**
     * 报表描述
     */
    private String description;

    /**
     * 查询条件
     */
    private Map<String, Object> queryConditions;

    /**
     * 汇总数据
     */
    private Map<String, Object> summaryData;

    /**
     * 图表数据
     */
    private List<ChartData> chartData;

    /**
     * 表格数据
     */
    private TableData tableData;

    /**
     * 生成时间
     */
    private LocalDateTime generateTime;

    /**
     * 数据时间范围
     */
    private DateRange dateRange;

    @Data
    public static class ChartData {
        private String chartType; // line, bar, pie, area, etc.
        private String title;
        private String xAxisTitle;
        private String yAxisTitle;
        private List<Series> series;
    }

    @Data
    public static class Series {
        private String name;
        private List<Object> data;
        private String type; // line, bar, etc.
        private Map<String, Object> options;
    }

    @Data
    public static class TableData {
        private List<String> headers;
        private List<List<Object>> rows;
        private Integer total;
        private Integer pageNum;
        private Integer pageSize;
    }

    @Data
    public static class DateRange {
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String period; // day, week, month, year
    }
}