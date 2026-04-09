package org.shyu.marketserviceuser.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.shyu.marketserviceuser.dto.RoleCreateRequest;
import org.shyu.marketserviceuser.dto.RoleUpdateRequest;
import org.shyu.marketserviceuser.dto.UserRoleAssignRequest;
import org.shyu.marketserviceuser.vo.RoleVO;
import org.shyu.marketserviceuser.vo.UserRoleVO;

import java.util.List;

/**
 * 角色管理服务接口
 */
public interface RoleManagementService {

    /**
     * 分页获取角色列表
     */
    Page<RoleVO> getRoles(Integer pageNum, Integer pageSize, String keyword);

    /**
     * 获取所有角色（不分页）
     */
    List<RoleVO> getAllRoles();

    /**
     * 根据ID获取角色详情
     */
    RoleVO getRoleDetail(Long roleId);

    /**
     * 创建角色
     */
    void createRole(RoleCreateRequest request);

    /**
     * 更新角色
     */
    void updateRole(Long roleId, RoleUpdateRequest request);

    /**
     * 删除角色
     */
    void deleteRole(Long roleId);

    /**
     * 启用/禁用角色
     */
    void updateRoleStatus(Long roleId, Integer status);

    /**
     * 为角色分配权限
     */
    void assignPermissions(Long roleId, List<Long> permissionIds);

    /**
     * 获取角色的权限列表
     */
    List<Long> getRolePermissions(Long roleId);

    /**
     * 为用户分配角色
     */
    void assignUserRoles(UserRoleAssignRequest request);

    /**
     * 移除用户角色
     */
    void removeUserRole(Long userId, Long roleId);

    /**
     * 获取用户角色列表
     */
    List<UserRoleVO> getUserRoles(Long userId);

    /**
     * 批量分配用户角色
     */
    void batchAssignUserRoles(List<UserRoleAssignRequest> requests);
}