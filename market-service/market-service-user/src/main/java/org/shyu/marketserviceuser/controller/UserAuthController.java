package org.shyu.marketserviceuser.controller;

import cn.dev33.satoken.stp.StpUtil;
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
import org.shyu.marketserviceuser.service.UserService;

/**
 * 用户认证控制器
 */
@Api(tags = "用户认证")
@RestController
@RequestMapping("/api/user/auth")
@RequiredArgsConstructor
public class UserAuthController {

    private final UserService userService;

    /**
     * 用户注册
     */
    @ApiOperation("用户注册")
    @PostMapping("/register")
    public Result<UserVO> register(@Validated @RequestBody UserRegisterDTO registerDTO) {
        UserVO userVO = userService.register(registerDTO);
        return Result.success("注册成功", userVO);
    }

    /**
     * 用户登录
     */
    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Validated @RequestBody UserLoginDTO loginDTO) {
        LoginVO loginVO = userService.login(loginDTO);
        return Result.success("登录成功", loginVO);
    }

    /**
     * 用户登出
     */
    @ApiOperation("用户登出")
    @PostMapping("/logout")
    public Result<String> logout() {
        // Sa-Token会自动处理登出逻辑
        StpUtil.logout();
        return Result.success("登出成功", null);
    }
}

