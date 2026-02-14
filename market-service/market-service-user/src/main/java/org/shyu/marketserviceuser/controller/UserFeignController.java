package org.shyu.marketserviceuser.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.shyu.marketapiuser.dto.UserDTO;
import org.shyu.marketserviceuser.entity.UserEntity;
import org.shyu.marketserviceuser.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

/**
 * 用户服务Feign接口实现
 * 提供内部服务调用的接口
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserFeignController {

    private final UserService userService;

    /**
     * 根据ID获取用户信息
     * @param id 用户ID
     * @return 用户信息（不包含密码）
     */
    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable("id") Long id) {
        UserEntity user = userService.getById(id);
        if (user == null || user.getStatus() == 0) {
            // status=0 表示已删除，返回null或者抛出异常
            return null;
        }
        return convertToDTO(user);
    }

    /**
     * 根据用户名获取用户信息
     * @param username 用户名
     * @return 用户信息（不包含密码）
     */
    @GetMapping("/username")
    public UserDTO getUserByUsername(@RequestParam("username") String username) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getUsername, username)
               .ne(UserEntity::getStatus, 0); // 排除已删除的用户
        UserEntity user = userService.getOne(wrapper);
        if (user == null) {
            return null;
        }
        return convertToDTO(user);
    }

    /**
     * 根据手机号获取用户信息
     * @param phone 手机号
     * @return 用户信息（不包含密码）
     */
    @GetMapping("/phone")
    public UserDTO getUserByPhone(@RequestParam("phone") String phone) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getPhone, phone)
               .eq(UserEntity::getStatus, 1); // 只返回正常用户
        UserEntity user = userService.getOne(wrapper);
        if (user == null) {
            return null;
        }
        return convertToDTO(user);
    }

    /**
     * 将 UserEntity 转换为 UserDTO（不包含密码等敏感信息）
     */
    private UserDTO convertToDTO(UserEntity user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setAvatar(user.getAvatar());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getEmail());
        dto.setStatus(user.getStatus());
        dto.setCreateTime(user.getCreateTime());
        dto.setUpdateTime(user.getUpdateTime());
        return dto;
    }
}

