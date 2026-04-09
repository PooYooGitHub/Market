package org.shyu.marketserviceuser.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 审核规则请求DTO
 *
 * @author shyu
 * @since 2026-04-05
 */
@Data
public class ModerationRuleRequest {

    /**
     * 规则名称
     */
    @NotBlank(message = "规则名称不能为空")
    private String ruleName;

    /**
     * 规则描述
     */
    private String description;

    /**
     * 内容类型：product, user_profile, comment, message, all
     */
    @NotNull(message = "内容类型不能为空")
    private String contentType;

    /**
     * 规则类型：keyword, regex, ai, composite
     */
    @NotNull(message = "规则类型不能为空")
    private String ruleType;

    /**
     * 规则内容/配置
     */
    private String ruleContent;

    /**
     * 规则参数
     */
    private Map<String, Object> ruleParams;

    /**
     * 触发条件
     */
    private List<String> triggerConditions;

    /**
     * 动作类型：auto_approve, auto_reject, manual_review, flag
     */
    @NotNull(message = "动作类型不能为空")
    private String actionType;

    /**
     * 风险等级：low, medium, high, critical
     */
    private String riskLevel = "medium";

    /**
     * 优先级
     */
    private Integer priority = 50;

    /**
     * 是否启用
     */
    private Boolean enabled = true;

    /**
     * 生效时间范围
     */
    private String effectiveTime;

    /**
     * 适用用户群体
     */
    private List<String> applicableUserGroups;

    /**
     * 排除条件
     */
    private List<String> excludeConditions;
}