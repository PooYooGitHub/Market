package org.shyu.marketserviceuser.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

/**
 * 审核动作请求DTO
 *
 * @author shyu
 * @since 2026-04-05
 */
@Data
public class ModerationActionRequest {

    /**
     * 审核动作：approve, reject, edit, delete, escalate
     */
    @NotNull(message = "审核动作不能为空")
    private String action;

    /**
     * 审核意见
     */
    private String comment;

    /**
     * 处理理由
     */
    private String reason;

    /**
     * 是否需要通知用户
     */
    private Boolean notifyUser = true;

    /**
     * 通知消息模板
     */
    private String notificationTemplate;

    /**
     * 额外处理参数
     */
    private String extraParams;

    /**
     * 后续动作：none, warn_user, suspend_user, ban_user
     */
    private String followUpAction;
}