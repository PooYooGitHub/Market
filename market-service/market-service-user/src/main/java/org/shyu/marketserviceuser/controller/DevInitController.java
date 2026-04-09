package org.shyu.marketserviceuser.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.shyu.marketcommon.result.Result;

import java.util.List;
import java.util.Map;

/**
 * 数据库初始化控制器 - 仅用于开发阶段
 * 生产环境应删除此控制器
 */
@Api(tags = "数据库初始化 - 开发工具")
@RestController
@RequestMapping("/dev")
@RequiredArgsConstructor
public class DevInitController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 初始化角色表数据
     */
    @ApiOperation("初始化角色表数据")
    @PostMapping("/init-roles")
    public Result<String> initRoles() {
        try {
            // 创建角色表数据
            String insertRolesSql = "INSERT INTO t_role (id, role_name, role_code, description, create_time) VALUES " +
                "(1, '管理员', 'ROLE_ADMIN', '系统管理员，拥有所有权限', NOW()), " +
                "(2, '普通用户', 'ROLE_USER', '普通用户，可以买卖商品', NOW()) " +
                "ON DUPLICATE KEY UPDATE role_name = VALUES(role_name), description = VALUES(description)";

            jdbcTemplate.execute(insertRolesSql);

            return Result.success("角色表初始化完成");
        } catch (Exception e) {
            return Result.error("角色表初始化失败: " + e.getMessage());
        }
    }

    /**
     * 为admin用户分配管理员角色
     */
    @ApiOperation("为admin用户分配管理员角色")
    @PostMapping("/assign-admin-role")
    public Result<String> assignAdminRole() {
        try {
            // 查找admin用户的ID
            String findAdminSql = "SELECT id FROM t_user WHERE username = 'admin'";
            List<Map<String, Object>> adminUsers = jdbcTemplate.queryForList(findAdminSql);

            if (adminUsers.isEmpty()) {
                return Result.error("未找到admin用户");
            }

            Long adminUserId = ((Number) adminUsers.get(0).get("id")).longValue();

            // 为admin分配管理员角色
            String assignRoleSql = "INSERT INTO t_user_role (user_id, role_id) VALUES (?, 1) " +
                "ON DUPLICATE KEY UPDATE role_id = VALUES(role_id)";

            jdbcTemplate.update(assignRoleSql, adminUserId);

            return Result.success("admin用户管理员角色分配完成，用户ID: " + adminUserId);
        } catch (Exception e) {
            return Result.error("角色分配失败: " + e.getMessage());
        }
    }

    /**
     * 查询用户角色信息
     */
    @ApiOperation("查询用户角色信息")
    @GetMapping("/user-roles")
    public Result<List<Map<String, Object>>> getUserRoles() {
        try {
            String sql = "SELECT u.id, u.username, u.nickname, r.role_code, r.role_name " +
                "FROM t_user u " +
                "LEFT JOIN t_user_role ur ON u.id = ur.user_id " +
                "LEFT JOIN t_role r ON ur.role_id = r.id " +
                "ORDER BY u.id";

            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
            return Result.success("查询成功", result);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
}