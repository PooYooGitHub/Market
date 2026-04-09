package org.shyu.marketserviceuser.service;

import org.shyu.marketserviceuser.dto.ReportQueryRequest;
import org.shyu.marketserviceuser.vo.DashboardVO;
import org.shyu.marketserviceuser.vo.ReportVO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 数据分析服务接口
 *
 * @author shyu
 * @since 2026-04-05
 */
public interface DataAnalysisService {

    /**
     * 获取管理员仪表盘数据
     */
    DashboardVO getDashboard();

    /**
     * 获取用户分析报表
     */
    ReportVO getUserAnalysisReport(ReportQueryRequest request);

    /**
     * 获取商品分析报表
     */
    ReportVO getProductAnalysisReport(ReportQueryRequest request);

    /**
     * 获取交易分析报表
     */
    ReportVO getTradeAnalysisReport(ReportQueryRequest request);

    /**
     * 获取财务分析报表
     */
    ReportVO getFinancialAnalysisReport(ReportQueryRequest request);

    /**
     * 获取运营分析报表
     */
    ReportVO getOperationAnalysisReport(ReportQueryRequest request);

    /**
     * 获取实时数据看板
     */
    Map<String, Object> getRealtimeDashboard();

    /**
     * 获取用户行为分析
     */
    Map<String, Object> getUserBehaviorAnalysis(String startDate, String endDate, String dimension);

    /**
     * 获取商品热力分析
     */
    Map<String, Object> getProductHeatmapAnalysis(String startDate, String endDate, String metric);

    /**
     * 获取漏斗分析数据
     */
    Map<String, Object> getFunnelAnalysis(String startDate, String endDate);

    /**
     * 获取留存分析数据
     */
    Map<String, Object> getRetentionAnalysis(String startDate, String endDate, String period);

    /**
     * 获取RFM分析数据
     */
    Map<String, Object> getRfmAnalysis(String startDate, String endDate);

    /**
     * 获取预测分析数据
     */
    Map<String, Object> getForecastAnalysis(String metric, Integer days);

    /**
     * 导出报表数据
     */
    String exportReport(String reportType, ReportQueryRequest request, String format);

    /**
     * 下载报表文件
     */
    void downloadReport(String fileName, HttpServletResponse response);

    /**
     * 获取自定义报表配置
     */
    List<Map<String, Object>> getCustomReports();

    /**
     * 保存自定义报表配置
     */
    void saveCustomReport(Map<String, Object> reportConfig);

    /**
     * 执行自定义报表
     */
    Map<String, Object> executeCustomReport(Long reportId, Map<String, Object> parameters);
}