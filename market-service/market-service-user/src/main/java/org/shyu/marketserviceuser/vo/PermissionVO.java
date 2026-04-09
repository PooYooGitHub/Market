package org.shyu.marketserviceuser.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限VO
 *
 * @author shyu
 * @since 2026-04-05
 */
@Data
public class PermissionVO {

    /**
     * 权限ID
     */
    private Long permissionId;

    /**
     * 父权限ID
     */
    private Long parentId;

    /**
     * 权限名称
     */
    private String permissionName;

    /**
     * 权限代码
     */
    private String permissionCode;

    /**
     * 权限类型：menu, button, api, data
     */
    private String permissionType;

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

    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status;

    /**
     * 层级
     */
    private Integer level;

    /**
     * 是否为系统权限
     */
    private Boolean isSystem;

    /**
     * 子权限列表
     */
    private List<PermissionVO> children;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}