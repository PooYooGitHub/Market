package org.shyu.marketserviceuser.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 权限更新请求DTO
 *
 * @author shyu
 * @since 2026-04-05
 */
@Data
public class PermissionUpdateRequest {

    /**
     * 权限名称
     */
    @NotBlank(message = "权限名称不能为空")
    private String permissionName;

    /**
     * 权限路径
     */
    private String permissionPath;

    /**
     * 请求方法：GET, POST, PUT, DELETE, *
     */
    private String requestMethod;

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
    private Integer sort;
}