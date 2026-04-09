package org.shyu.marketserviceuser.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 权限创建请求DTO
 *
 * @author shyu
 * @since 2026-04-05
 */
@Data
public class PermissionCreateRequest {

    /**
     * 父权限ID（0表示顶级权限）
     */
    @NotNull(message = "父权限ID不能为空")
    private Long parentId;

    /**
     * 权限名称
     */
    @NotBlank(message = "权限名称不能为空")
    private String permissionName;

    /**
     * 权限代码
     */
    @NotBlank(message = "权限代码不能为空")
    private String permissionCode;

    /**
     * 权限类型：menu, button, api, data
     */
    @NotBlank(message = "权限类型不能为空")
    private String permissionType;

    /**
     * 权限路径
     */
    private String permissionPath;

    /**
     * 请求方法：GET, POST, PUT, DELETE, *
     */
    private String requestMethod = "*";

    /**
     * 权限描述
     */
    private String description;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序
     */
    private Integer sort = 0;
}