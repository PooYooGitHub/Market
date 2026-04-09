package org.shyu.marketserviceuser.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 审核统计数据VO
 *
 * @author shyu
 * @since 2026-04-05
 */
@Data
public class ModerationStatisticsVO {

    /**
     * 总审核任务数
     */
    private Long totalTasks;

    /**
     * 待处理任务数
     */
    private Long pendingTasks;

    /**
     * 已处理任务数
     */
    private Long processedTasks;

    /**
     * 通过任务数
     */
    private Long approvedTasks;

    /**
     * 拒绝任务数
     */
    private Long rejectedTasks;

    /**
     * 今日新增任务数
     */
    private Long todayNewTasks;

    /**
     * 今日处理任务数
     */
    private Long todayProcessedTasks;

    /**
     * 平均处理时间（分钟）
     */
    private Double averageProcessTime;

    /**
     * 审核通过率
     */
    private Double approvalRate;

    /**
     * 自动审核率
     */
    private Double autoModerationRate;

    /**
     * 高风险内容数量
     */
    private Long highRiskContent;

    /**
     * 敏感词命中次数
     */
    private Long sensitiveWordHits;

    /**
     * 活跃审核员数量
     */
    private Long activeModerators;

    /**
     * 内容类型分布
     */
    private ContentTypeStats contentTypeStats;

    /**
     * 风险等级分布
     */
    private RiskLevelStats riskLevelStats;

    /**
     * 统计时间
     */
    private LocalDateTime statisticsTime;

    @Data
    public static class ContentTypeStats {
        private Long productCount;
        private Long userProfileCount;
        private Long commentCount;
        private Long messageCount;
    }

    @Data
    public static class RiskLevelStats {
        private Long safeCount;
        private Long lowRiskCount;
        private Long mediumRiskCount;
        private Long highRiskCount;
        private Long criticalCount;
    }
}