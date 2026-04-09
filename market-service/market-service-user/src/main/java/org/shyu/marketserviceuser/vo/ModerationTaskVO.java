package org.shyu.marketserviceuser.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 审核任务VO
 *
 * @author shyu
 * @since 2026-04-05
 */
@Data
public class ModerationTaskVO {

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 内容类型：product, user_profile, comment, message
     */
    private String contentType;

    /**
     * 内容ID
     */
    private Long contentId;

    /**
     * 内容标题
     */
    private String contentTitle;

    /**
     * 内容摘要
     */
    private String contentSummary;

    /**
     * 内容详情
     */
    private String contentDetail;

    /**
     * 检测结果：safe, risk, violation
     */
    private String detectionResult;

    /**
     * 风险等级：low, medium, high, critical
     */
    private String riskLevel;

    /**
     * 触发规则
     */
    private String triggeredRules;

    /**
     * 敏感词匹配结果
     */
    private String sensitiveWords;

    /**
     * 任务状态：pending, processing, approved, rejected
     */
    private String status;

    /**
     * 审核员ID
     */
    private Long moderatorId;

    /**
     * 审核员姓名
     */
    private String moderatorName;

    /**
     * 审核意见
     */
    private String moderationComment;

    /**
     * 处理动作：approve, reject, edit, delete
     */
    private String action;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 处理时间
     */
    private LocalDateTime processTime;

    /**
     * 优先级：low, normal, high, urgent
     */
    private String priority;

    /**
     * 来源：auto, manual, report
     */
    private String source;
}