package org.shyu.marketserviceuser.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.shyu.marketapiuser.dto.UserDTO;
import org.shyu.marketcommon.result.Result;
import org.shyu.marketserviceuser.entity.UserEntity;
import org.shyu.marketserviceuser.service.UserService;
import org.springframework.web.bind.annotation.*;

/**
 * User Feign Controller
 * Provides internal service call interfaces (for Auth Service and other services)
 *
 * 注意：Gateway 配置了 StripPrefix=1，会移除 /api 前缀
 * 所以这里的 RequestMapping 是 /user，而不是 /api/user
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserFeignController {

    private final UserService userService;

    /**
     * Get user by ID
     * @param id user ID
     * @return user info (without password)
     */
    @GetMapping("/{id}")
    public Result<UserDTO> getUserById(@PathVariable("id") Long id) {
        UserEntity user = userService.getById(id);
        if (user == null || user.getStatus() == 0) {
            return Result.error("User not found");
        }
        return Result.success(convertToDTO(user));
    }

    /**
     * Get user by username
     * @param username username
     * @return user info (without password)
     */
    @GetMapping("/username")
    public Result<UserDTO> getUserByUsername(@RequestParam("username") String username) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getUsername, username)
               .ne(UserEntity::getStatus, 0);
        UserEntity user = userService.getOne(wrapper);
        if (user == null) {
            return Result.error("User not found");
        }
        return Result.success(convertToDTO(user));
    }

    /**
     * Get user by phone
     * @param phone phone number
     * @return user info (without password)
     */
    @GetMapping("/phone")
    public Result<UserDTO> getUserByPhone(@RequestParam("phone") String phone) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getPhone, phone)
               .eq(UserEntity::getStatus, 1);
        UserEntity user = userService.getOne(wrapper);
        if (user == null) {
            return Result.error("User not found");
        }
        return Result.success(convertToDTO(user));
    }

    /**
     * 获取用户统计数据
     * @return 用户统计信息
     */
    @GetMapping("/statistics")
    public Result<org.shyu.marketserviceuser.vo.UserStatisticsVO> getUserStatistics() {
        // 统计总用户数
        Long totalCount = userService.count();

        // 统计正常用户数（status=1）
        LambdaQueryWrapper<UserEntity> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.eq(UserEntity::getStatus, 1);
        Long activeCount = userService.count(activeWrapper);

        // 统计禁用用户数（status=0）
        LambdaQueryWrapper<UserEntity> disabledWrapper = new LambdaQueryWrapper<>();
        disabledWrapper.eq(UserEntity::getStatus, 0);
        Long disabledCount = userService.count(disabledWrapper);

        // 统计今日注册数
        LambdaQueryWrapper<UserEntity> todayWrapper = new LambdaQueryWrapper<>();
        java.time.LocalDateTime startOfDay = java.time.LocalDateTime.now().toLocalDate().atStartOfDay();
        java.time.LocalDateTime endOfDay = startOfDay.plusDays(1);
        todayWrapper.between(UserEntity::getCreateTime, startOfDay, endOfDay);
        Long todayRegister = userService.count(todayWrapper);

        org.shyu.marketserviceuser.vo.UserStatisticsVO statistics = new org.shyu.marketserviceuser.vo.UserStatisticsVO();
        statistics.setTotalCount(totalCount);
        statistics.setActiveCount(activeCount);
        statistics.setDisabledCount(disabledCount);
        statistics.setDeletedCount(0L); // 物理删除无法统计
        statistics.setTodayRegister(todayRegister);

        return Result.success(statistics);
    }

    /**
     * Convert UserEntity to UserDTO (exclude sensitive info like password)
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

