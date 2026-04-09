package org.shyu.marketserviceuser.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 角色更新请求DTO
 *
 * @author shyu
 * @since 2026-04-05
 */
@Data
public class RoleUpdateRequest {

    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 角色级别
     */
    private Integer roleLevel;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 是否为默认角色
     */
    private Boolean isDefault;

    /**
     * 权限ID列表
     */
    private List<Long> permissionIds;
}