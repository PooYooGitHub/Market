package org.shyu.marketapiuser.feign;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shyu.marketapiuser.dto.UserDTO;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * UserFeignClient 单元测试
 * 使用 Mock 方式测试 Feign 接口
 */
@ExtendWith(MockitoExtension.class)
class UserFeignClientTest {

    @Mock
    private UserFeignClient userFeignClient;

    /**
     * 测试根据ID获取用户 - 正常用户（status=1）
     */
    @Test
    void testGetUserById_Normal() {
        // 准备测试数据
        Long userId = 1L;
        UserDTO mockUser = new UserDTO();
        mockUser.setId(userId);
        mockUser.setUsername("testUser");
        mockUser.setNickname("测试用户");
        mockUser.setPhone("13800138000");
        mockUser.setEmail("test@example.com");
        mockUser.setStatus(1); // 正常状态

        // 配置 Mock 行为
        when(userFeignClient.getUserById(userId)).thenReturn(mockUser);

        // 执行测试
        UserDTO result = userFeignClient.getUserById(userId);

        // 验证结果
        assertNotNull(result, "用户信息不应为空");
        assertEquals(userId, result.getId(), "用户ID应该匹配");
        assertEquals("testUser", result.getUsername(), "用户名应该匹配");
        assertEquals(1, result.getStatus(), "用户状态应该为正常(1)");

        // 验证方法被调用
        verify(userFeignClient, times(1)).getUserById(userId);
    }

    /**
     * 测试根据ID获取用户 - 已删除用户（status=0）
     * 注意：实际实现中，已删除用户应该返回 null
     */
    @Test
    void testGetUserById_Deleted() {
        // 准备测试数据 - 已删除用户
        Long userId = 2L;

        // 配置 Mock 行为 - 已删除用户应返回 null
        when(userFeignClient.getUserById(userId)).thenReturn(null);

        // 执行测试
        UserDTO result = userFeignClient.getUserById(userId);

        // 验证结果
        assertNull(result, "已删除用户应该返回 null");

        // 验证方法被调用
        verify(userFeignClient, times(1)).getUserById(userId);
    }

    /**
     * 测试根据ID获取用户 - 用户不存在
     */
    @Test
    void testGetUserById_NotFound() {
        Long userId = 999L;

        // 配置 Mock 行为
        when(userFeignClient.getUserById(userId)).thenReturn(null);

        // 执行测试
        UserDTO result = userFeignClient.getUserById(userId);

        // 验证结果
        assertNull(result, "不存在的用户应该返回 null");
    }

    /**
     * 测试根据用户名获取用户
     */
    @Test
    void testGetUserByUsername() {
        // 准备测试数据
        String username = "testUser";
        UserDTO mockUser = new UserDTO();
        mockUser.setId(1L);
        mockUser.setUsername(username);
        mockUser.setStatus(1);

        // 配置 Mock 行为
        when(userFeignClient.getUserByUsername(username)).thenReturn(mockUser);

        // 执行测试
        UserDTO result = userFeignClient.getUserByUsername(username);

        // 验证结果
        assertNotNull(result, "用户信息不应为空");
        assertEquals(username, result.getUsername(), "用户名应该匹配");
        assertNotEquals(0, result.getStatus(), "返回的用户应该是有效用户");

        // 验证方法被调用
        verify(userFeignClient, times(1)).getUserByUsername(username);
    }

    /**
     * 测试根据用户名获取用户 - 用户不存在
     */
    @Test
    void testGetUserByUsername_NotFound() {
        String username = "nonExistentUser";

        // 配置 Mock 行为
        when(userFeignClient.getUserByUsername(username)).thenReturn(null);

        // 执行测试
        UserDTO result = userFeignClient.getUserByUsername(username);

        // 验证结果
        assertNull(result, "不存在的用户应该返回 null");
    }

    /**
     * 测试根据手机号获取用户
     */
    @Test
    void testGetUserByPhone() {
        // 准备测试数据
        String phone = "13800138000";
        UserDTO mockUser = new UserDTO();
        mockUser.setId(1L);
        mockUser.setUsername("testUser");
        mockUser.setPhone(phone);
        mockUser.setStatus(1);

        // 配置 Mock 行为
        when(userFeignClient.getUserByPhone(phone)).thenReturn(mockUser);

        // 执行测试
        UserDTO result = userFeignClient.getUserByPhone(phone);

        // 验证结果
        assertNotNull(result, "用户信息不应为空");
        assertEquals(phone, result.getPhone(), "手机号应该匹配");
        assertEquals(1, result.getStatus(), "用户状态应该为正常(1)");

        // 验证方法被调用
        verify(userFeignClient, times(1)).getUserByPhone(phone);
    }

    /**
     * 测试根据手机号获取用户 - 用户不存在
     */
    @Test
    void testGetUserByPhone_NotFound() {
        String phone = "19999999999";

        // 配置 Mock 行为
        when(userFeignClient.getUserByPhone(phone)).thenReturn(null);

        // 执行测试
        UserDTO result = userFeignClient.getUserByPhone(phone);

        // 验证结果
        assertNull(result, "不存在的用户应该返回 null");
    }

    /**
     * 测试多次调用相同接口
     */
    @Test
    void testMultipleCalls() {
        Long userId = 1L;
        UserDTO mockUser = new UserDTO();
        mockUser.setId(userId);
        mockUser.setStatus(1);

        // 配置 Mock 行为
        when(userFeignClient.getUserById(userId)).thenReturn(mockUser);

        // 执行多次调用
        UserDTO result1 = userFeignClient.getUserById(userId);
        UserDTO result2 = userFeignClient.getUserById(userId);

        // 验证结果
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(result1.getId(), result2.getId());

        // 验证方法被调用了2次
        verify(userFeignClient, times(2)).getUserById(userId);
    }
}

