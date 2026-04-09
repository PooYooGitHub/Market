package org.shyu.marketserviceuser.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.util.Map;

/**
 * 系统配置请求DTO
 *
 * @author shyu
 * @since 2026-04-05
 */
@Data
public class SystemConfigRequest {

    /**
     * 配置类型：system, security, notification, etc.
     */
    @NotBlank(message = "配置类型不能为空")
    private String configType;

    /**
     * 配置数据
     */
    private Map<String, Object> configData;

    /**
     * 配置描述
     */
    private String description;

    /**
     * 是否立即生效
     */
    private Boolean immediateEffect = true;
}