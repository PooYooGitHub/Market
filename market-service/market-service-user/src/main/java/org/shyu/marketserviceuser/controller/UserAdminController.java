package org.shyu.marketserviceuser.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.shyu.marketapiuser.vo.UserVO;
import org.shyu.marketcommon.result.Result;
import org.shyu.marketserviceuser.dto.ChangePasswordDTO;
import org.shyu.marketserviceuser.dto.UserQueryDTO;
import org.shyu.marketserviceuser.entity.UserEntity;
import org.shyu.marketserviceuser.service.UserService;
import org.shyu.marketserviceuser.vo.UserStatisticsVO;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理控制器
 * 用于管理员管理用户
 *
 * 注意：Gateway 配置了 StripPrefix=1，会移除 /api 前缀
 * 所以这里的 RequestMapping 是 /user/admin，而不是 /api/user/admin
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/user/admin")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserService userService;

    /**
     * 分页查询用户列表
     */
    @ApiOperation("分页查询用户列表")
    @GetMapping("/list")
    public Result<Page<UserVO>> listUsers(UserQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索（用户名、昵称、手机号）
        if (StrUtil.isNotBlank(queryDTO.getKeyword())) {
            wrapper.and(w -> w
                .like(UserEntity::getUsername, queryDTO.getKeyword())
                .or().like(UserEntity::getNickname, queryDTO.getKeyword())
                .or().like(UserEntity::getPhone, queryDTO.getKeyword())
            );
        }

        // 状态筛选
        if (queryDTO.getStatus() != null) {
            wrapper.eq(UserEntity::getStatus, queryDTO.getStatus());
        }

        // 时间范围筛选
        if (queryDTO.getStartTime() != null) {
            wrapper.ge(UserEntity::getCreateTime, queryDTO.getStartTime());
        }
        if (queryDTO.getEndTime() != null) {
            wrapper.le(UserEntity::getCreateTime, queryDTO.getEndTime());
        }

        // 排序
        wrapper.orderByDesc(UserEntity::getCreateTime);

        // 分页查询
        Page<UserEntity> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<UserEntity> userPage = userService.page(page, wrapper);

        // 转换为VO
        Page<UserVO> resultPage = new Page<>();
        BeanUtil.copyProperties(userPage, resultPage, "records");
        List<UserVO> voList = userPage.getRecords().stream()
            .map(user -> {
                UserVO vo = BeanUtil.copyProperties(user, UserVO.class);
                // 不返回密码
                return vo;
            })
            .collect(Collectors.toList());
        resultPage.setRecords(voList);

        return Result.success(resultPage);
    }

    /**
     * 根据ID查询用户详情
     */
    @ApiOperation("查询用户详情")
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        UserEntity user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
        return Result.success(userVO);
    }

    /**
     * 启用用户
     */
    @ApiOperation("启用用户")
    @PutMapping("/{id}/enable")
    public Result<String> enableUser(@PathVariable Long id) {
        UserEntity user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        user.setStatus(1); // 1表示正常
        userService.updateById(user);

        return Result.success("用户已启用");
    }

    /**
     * 禁用用户
     */
    @ApiOperation("禁用用户")
    @PutMapping("/{id}/disable")
    public Result<String> disableUser(@PathVariable Long id) {
        UserEntity user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        user.setStatus(2); // 2表示禁用
        userService.updateById(user);

        // 踢下线
        StpUtil.kickout(id);

        return Result.success("用户已禁用");
    }

    /**
     * 删除用户（软删除）
     */
    @ApiOperation("删除用户")
    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        UserEntity user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 软删除：设置status=0
        user.setStatus(0);
        userService.updateById(user);

        // 踢下线
        StpUtil.kickout(id);

        return Result.success("用户已删除");
    }

    /**
     * 重置用户密码
     */
    @ApiOperation("重置用户密码")
    @PutMapping("/{id}/reset-password")
    public Result<String> resetPassword(@PathVariable Long id, @RequestParam String newPassword) {
        boolean success = userService.resetPassword(id, newPassword);
        if (!success) {
            return Result.error("密码重置失败");
        }

        // 踢下线，要求重新登录
        StpUtil.kickout(id);

        return Result.success("密码已重置");
    }

    /**
     * 批量删除用户（物理删除）
     */
    @ApiOperation("批量删除用户")
    @DeleteMapping("/batch")
    public Result<String> batchDelete(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error("请选择要删除的用户");
        }

        // 物理删除
        userService.removeByIds(ids);

        // 批量踢下线
        ids.forEach(StpUtil::kickout);

        return Result.success("批量删除成功");
    }

    /**
     * 统计用户数据
     */
    @ApiOperation("统计用户数据")
    @GetMapping("/statistics")
    public Result<UserStatisticsVO> getStatistics() {
        UserStatisticsVO statistics = userService.getStatistics();
        return Result.success(statistics);
    }
}


