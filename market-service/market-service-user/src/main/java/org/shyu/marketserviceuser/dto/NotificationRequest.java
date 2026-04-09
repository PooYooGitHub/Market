package org.shyu.marketserviceuser.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 系统通知请求DTO
 *
 * @author shyu
 * @since 2026-04-05
 */
@Data
public class NotificationRequest {

    /**
     * 通知标题
     */
    @NotBlank(message = "通知标题不能为空")
    private String title;

    /**
     * 通知内容
     */
    @NotBlank(message = "通知内容不能为空")
    private String content;

    /**
     * 通知类型：system, announcement, warning, error
     */
    @NotNull(message = "通知类型不能为空")
    private String type;

    /**
     * 发送方式：all(全体用户), specific(指定用户), role(指定角色)
     */
    @NotNull(message = "发送方式不能为空")
    private String sendMode;

    /**
     * 目标用户ID列表（sendMode=specific时使用）
     */
    private List<Long> targetUserIds;

    /**
     * 目标角色列表（sendMode=role时使用）
     */
    private List<String> targetRoles;

    /**
     * 是否立即发送
     */
    private Boolean sendImmediately = true;

    /**
     * 定时发送时间（sendImmediately=false时使用）
     */
    private String scheduledTime;

    /**
     * 通知优先级：low, normal, high, urgent
     */
    private String priority = "normal";
}