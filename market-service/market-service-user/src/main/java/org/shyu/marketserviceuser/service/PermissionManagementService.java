package org.shyu.marketserviceuser.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.shyu.marketserviceuser.dto.PermissionCreateRequest;
import org.shyu.marketserviceuser.dto.PermissionUpdateRequest;
import org.shyu.marketserviceuser.vo.PermissionVO;

import java.util.List;

/**
 * 权限管理服务接口
 */
public interface PermissionManagementService {

    /**
     * 获取权限树
     */
    List<PermissionVO> getPermissionTree();

    /**
     * 分页获取权限列表
     */
    Page<PermissionVO> getPermissions(Integer pageNum, Integer pageSize, String keyword, String type);

    /**
     * 根据ID获取权限详情
     */
    PermissionVO getPermissionDetail(Long permissionId);

    /**
     * 创建权限
     */
    void createPermission(PermissionCreateRequest request);

    /**
     * 更新权限
     */
    void updatePermission(Long permissionId, PermissionUpdateRequest request);

    /**
     * 删除权限
     */
    void deletePermission(Long permissionId);

    /**
     * 启用/禁用权限
     */
    void updatePermissionStatus(Long permissionId, Integer status);

    /**
     * 获取所有权限（不分页）
     */
    List<PermissionVO> getAllPermissions();

    /**
     * 根据角色ID获取权限列表
     */
    List<PermissionVO> getPermissionsByRoleId(Long roleId);

    /**
     * 根据用户ID获取权限列表
     */
    List<PermissionVO> getPermissionsByUserId(Long userId);
}