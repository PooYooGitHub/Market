package org.shyu.marketserviceuser.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.shyu.marketapiuser.dto.UserLoginDTO;
import org.shyu.marketapiuser.vo.LoginVO;
import org.shyu.marketapiuser.vo.UserVO;
import org.shyu.marketcommon.result.Result;
import org.shyu.marketserviceuser.entity.UserEntity;
import org.shyu.marketserviceuser.service.UserService;

import java.util.List;

/**
 * 管理员控制器
 * 专门处理管理员相关的功能
 */
@Api(tags = "管理员功能")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    /**
     * 管理员登录
     * 白名单接口，无需登录
     */
    @ApiOperation("管理员登录")
    @PostMapping("/login")
    public Result<LoginVO> adminLogin(@Validated @RequestBody UserLoginDTO loginDTO) {
        LoginVO loginVO = userService.login(loginDTO);

        // 检查用户是否有管理员权限
        UserVO userInfo = loginVO.getUserInfo();
        List<String> roles = userService.getUserRoles(userInfo.getId());

        if (roles == null || (!roles.contains("ADMIN") && !roles.contains("ARBITRATOR"))) {
            return Result.error("您没有管理员权限");
        }

        // 手动设置角色信息到返回结果中
        // userInfo.setRoles(roles); // 暂时注释掉，避免编译错误

        return Result.success("管理员登录成功", loginVO);
    }

    /**
     * 检查用户是否为管理员
     */
    @ApiOperation("检查管理员权限")
    @GetMapping("/check-auth")
    public Result<Boolean> checkAdminAuth(@RequestHeader("X-User-Id") Long userId) {
        List<String> roles = userService.getUserRoles(userId);
        boolean isAdmin = roles != null && (roles.contains("ADMIN") || roles.contains("ARBITRATOR"));
        return Result.success(isAdmin);
    }

    /**
     * 获取当前管理员信息
     */
    @ApiOperation("获取当前管理员信息")
    @GetMapping("/info")
    public Result<UserVO> getCurrentAdmin(@RequestHeader("X-User-Id") Long userId) {
        // 先检查管理员权限
        List<String> roles = userService.getUserRoles(userId);
        if (roles == null || (!roles.contains("ADMIN") && !roles.contains("ARBITRATOR"))) {
            return Result.error("您没有管理员权限");
        }

        // 获取用户信息
        UserEntity user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        UserVO userVO = new UserVO();
        userVO.setId(userId);
        userVO.setUsername(user.getUsername());
        userVO.setNickname(user.getNickname());
        // userVO.setRoles(roles); // 暂时注释掉，避免编译错误

        return Result.success(userVO);
    }
}