package org.shyu.marketserviceuser.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.shyu.marketapiuser.dto.UserLoginDTO;
import org.shyu.marketapiuser.dto.UserRegisterDTO;
import org.shyu.marketapiuser.vo.LoginVO;
import org.shyu.marketapiuser.vo.UserVO;
import org.shyu.marketcommon.result.Result;
import org.shyu.marketserviceuser.dto.ChangePasswordDTO;
import org.shyu.marketserviceuser.entity.UserEntity;
import org.shyu.marketserviceuser.service.UserService;
import cn.hutool.core.bean.BeanUtil;

/**
 * 用户信息控制器
 * 供前端直接调用的接口
 */
@Api(tags = "用户信息")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户注册
     * 白名单接口，无需登录
     */
    @ApiOperation("用户注册")
    @PostMapping("/register")
    public Result<Long> register(@Validated @RequestBody UserRegisterDTO registerDTO) {
        Long userId = userService.register(registerDTO);
        return Result.success("注册成功", userId);
    }

    /**
     * 用户登录
     * 白名单接口，无需登录
     * 登录成功后返回 Token
     */
    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Validated @RequestBody UserLoginDTO loginDTO) {
        LoginVO loginVO = userService.login(loginDTO);
        return Result.success("登录成功", loginVO);
    }

    /**
     * 获取当前用户信息
     * Gateway 已验证 Token，从 Header 获取 userId
     */
    @ApiOperation("获取当前用户信息")
    @GetMapping("/info")
    public Result<UserVO> getCurrentUser(@RequestHeader("X-User-Id") Long userId) {
        UserEntity user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
        return Result.success(userVO);
    }

    /**
     * 更新用户信息
     * Gateway 已验证 Token，从 Header 获取 userId
     */
    @ApiOperation("更新用户信息")
    @PutMapping("/update")
    public Result<UserVO> updateUser(@RequestHeader("X-User-Id") Long userId,
                                     @RequestBody UserVO userVO) {
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
     * Gateway 已验证 Token，从 Header 获取 userId
     */
    @ApiOperation("修改密码")
    @PutMapping("/change-password")
    public Result<String> changePassword(@RequestHeader("X-User-Id") Long userId,
                                         @Validated @RequestBody ChangePasswordDTO dto) {
        // 验证两次密码是否一致
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            return Result.error("两次密码输入不一致");
        }

        boolean success = userService.changePassword(userId, dto.getOldPassword(), dto.getNewPassword());

        if (success) {
            return Result.success("密码修改成功，请重新登录");
        } else {
            return Result.error("密码修改失败");
        }
    }
}

