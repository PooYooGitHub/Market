package org.shyu.marketapiuser.feign;

import org.junit.jupiter.api.Test;
import org.shyu.marketapiuser.dto.UserDTO;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserFeignClient 集成测试
 * 需要启动 market-service-core 服务后才能运行
 * 这个测试会真实调用远程服务接口
 *
 * 运行前提：
 * 1. 启动 Nacos 服务
 * 2. 启动 market-service-core 服务
 * 3. 数据库中有测试数据
 */
// @SpringBootTest // 取消注释以启用集成测试
// @EnableFeignClients(basePackages = "org.shyu.marketapiuser.feign")
class UserFeignClientIntegrationTest {

    // @Autowired
    private UserFeignClient userFeignClient;

    /**
     * 测试根据ID获取用户 - 真实调用
     * 注意：需要数据库中存在 ID=1 的用户
     */
    @Test
    void testGetUserById_RealCall() {
        // 这是一个示例，实际测试时需要确保数据库中有对应的测试数据
        Long userId = 1L;

        UserDTO user = userFeignClient.getUserById(userId);

        if (user != null) {
            assertNotNull(user.getId(), "用户ID不应为空");
            assertNotNull(user.getUsername(), "用户名不应为空");
            assertNotEquals(0, user.getStatus(), "用户应该是有效用户（status != 0）");
            System.out.println("获取到用户信息: " + user.getUsername());
        } else {
            System.out.println("用户不存在或已删除");
        }
    }

    /**
     * 测试根据用户名获取用户 - 真实调用
     */
    @Test
    void testGetUserByUsername_RealCall() {
        String username = "testUser"; // 需要修改为实际存在的用户名

        UserDTO user = userFeignClient.getUserByUsername(username);

        if (user != null) {
            assertEquals(username, user.getUsername(), "用户名应该匹配");
            assertNotEquals(0, user.getStatus(), "用户应该是有效用户");
            System.out.println("通过用户名找到用户: ID=" + user.getId());
        } else {
            System.out.println("用户名不存在或已删除");
        }
    }

    /**
     * 测试根据手机号获取用户 - 真实调用
     */
    @Test
    void testGetUserByPhone_RealCall() {
        String phone = "13800138000"; // 需要修改为实际存在的手机号

        UserDTO user = userFeignClient.getUserByPhone(phone);

        if (user != null) {
            assertEquals(phone, user.getPhone(), "手机号应该匹配");
            assertNotEquals(0, user.getStatus(), "用户应该是有效用户");
            System.out.println("通过手机号找到用户: " + user.getUsername());
        } else {
            System.out.println("手机号不存在或已删除");
        }
    }

    /**
     * 测试已删除用户 - 真实调用
     * 注意：需要数据库中有 status=0 的测试数据
     */
    @Test
    void testGetDeletedUser_RealCall() {
        // 假设 ID=999 的用户已被删除（status=0）
        Long deletedUserId = 999L;

        UserDTO user = userFeignClient.getUserById(deletedUserId);

        // 已删除的用户应该返回 null
        assertNull(user, "已删除的用户应该返回 null");
        System.out.println("正确处理了已删除用户");
    }
}

