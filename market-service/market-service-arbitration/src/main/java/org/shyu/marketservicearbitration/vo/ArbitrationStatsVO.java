package org.shyu.marketservicearbitration.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 仲裁统计展示对象
 * @author shyu
 * @since 2026-04-01
 */
@Data
@ApiModel("仲裁统计展示对象")
public class ArbitrationStatsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("统计日期")
    private String date;

    @ApiModelProperty("总申请数")
    private Integer totalCount = 0;

    @ApiModelProperty("待处理数")
    private Integer pendingCount = 0;

    @ApiModelProperty("处理中数")
    private Integer processingCount = 0;

    @ApiModelProperty("已完结数")
    private Integer completedCount = 0;

    @ApiModelProperty("已驳回数")
    private Integer rejectedCount = 0;

    @ApiModelProperty("成功申请数")
    private Integer successfulCount = 0;

    @ApiModelProperty("成功率")
    private Double successRate = 0.0;

    // 新增字段，用于前端Dashboard显示
    @ApiModelProperty("总案件数")
    private Integer totalCases = 0;

    @ApiModelProperty("已解决案件数")
    private Integer resolvedCount = 0;

    @ApiModelProperty("紧急案件数")
    private Integer urgentCount = 0;

    @ApiModelProperty("今日新增数")
    private Integer todayCount = 0;

    @ApiModelProperty("平均处理天数")
    private Double avgProcessDays = 0.0;

    @ApiModelProperty("平均等待天数")
    private Double avgWaitDays = 0.0;

    @ApiModelProperty("今日新增案件数")
    private Integer todayNewCount = 0;

    @ApiModelProperty("平均处理天数")
    private Double avgHandleDays = 0.0;

    @ApiModelProperty("类型分布数据")
    private List<Map<String, Object>> typeDistribution;

    @ApiModelProperty("月度趋势数据")
    private List<Map<String, Object>> monthlyTrend;

    @ApiModelProperty("周统计数据")
    private List<Map<String, Object>> weeklyStats;

    @ApiModelProperty("效率数据")
    private List<Map<String, Object>> efficiencyData;

}