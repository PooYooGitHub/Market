package org.shyu.marketserviceuser.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 角色创建请求DTO
 *
 * @author shyu
 * @since 2026-04-05
 */
@Data
public class RoleCreateRequest {

    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    /**
     * 角色代码
     */
    @NotBlank(message = "角色代码不能为空")
    private String roleCode;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 角色类型：system, custom
     */
    private String roleType = "custom";

    /**
     * 角色级别
     */
    private Integer roleLevel = 1;

    /**
     * 排序
     */
    private Integer sort = 0;

    /**
     * 是否为默认角色
     */
    private Boolean isDefault = false;

    /**
     * 权限ID列表
     */
    private List<Long> permissionIds;
}