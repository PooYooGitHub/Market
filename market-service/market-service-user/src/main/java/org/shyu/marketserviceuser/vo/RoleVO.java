package org.shyu.marketserviceuser.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色VO
 *
 * @author shyu
 * @since 2026-04-05
 */
@Data
public class RoleVO {

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色代码
     */
    private String roleCode;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 角色类型：system, custom
     */
    private String roleType;

    /**
     * 角色级别
     */
    private Integer roleLevel;

    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 是否为默认角色
     */
    private Boolean isDefault;

    /**
     * 权限列表
     */
    private List<PermissionVO> permissions;

    /**
     * 用户数量
     */
    private Long userCount;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 创建者姓名
     */
    private String creatorName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}