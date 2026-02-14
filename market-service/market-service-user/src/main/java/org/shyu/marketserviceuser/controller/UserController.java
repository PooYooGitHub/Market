package org.shyu.marketserviceuser.controller;

import cn.dev33.satoken.stp.StpUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.shyu.marketapiuser.vo.UserVO;
import org.shyu.marketcommon.result.Result;
import org.shyu.marketserviceuser.dto.ChangePasswordDTO;
import org.shyu.marketserviceuser.entity.UserEntity;
import org.shyu.marketserviceuser.service.UserService;
import cn.hutool.core.bean.BeanUtil;

/**
 * 用户信息控制器
 */
@Api(tags = "用户信息")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取当前用户信息
     */
    @ApiOperation("获取当前用户信息")
    @GetMapping("/current")
    public Result<UserVO> getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        UserEntity user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
        return Result.success(userVO);
    }

    /**
     * 更新用户信息
     */
    @ApiOperation("更新用户信息")
    @PutMapping("/update")
    public Result<UserVO> updateUser(@RequestBody UserVO userVO) {
        Long userId = StpUtil.getLoginIdAsLong();
        UserEntity user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 只允许更新部分字段
        if (userVO.getNickname() != null) {
            user.setNickname(userVO.getNickname());
        }
        if (userVO.getAvatar() != null) {
            user.setAvatar(userVO.getAvatar());
        }
        if (userVO.getEmail() != null) {
            user.setEmail(userVO.getEmail());
        }

        userService.updateById(user);
        UserVO result = BeanUtil.copyProperties(user, UserVO.class);
        return Result.success("更新成功", result);
    }

    /**
     * 修改密码
     */
    @ApiOperation("修改密码")
    @PutMapping("/change-password")
    public Result<String> changePassword(@Validated @RequestBody ChangePasswordDTO dto) {
        // 验证两次密码是否一致
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            return Result.error("两次密码输入不一致");
        }

        Long userId = StpUtil.getLoginIdAsLong();
        boolean success = userService.changePassword(userId, dto.getOldPassword(), dto.getNewPassword());

        if (success) {
            return Result.success("密码修改成功，请重新登录");
        } else {
            return Result.error("密码修改失败");
        }
    }

    /**
     * 检查登录状态
     */
    @ApiOperation("检查登录状态")
    @GetMapping("/check")
    public Result<Boolean> checkLogin() {
        boolean isLogin = StpUtil.isLogin();
        return Result.success(isLogin);
    }
}

