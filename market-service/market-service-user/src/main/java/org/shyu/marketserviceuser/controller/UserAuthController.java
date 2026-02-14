package org.shyu.marketserviceuser.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shyu.marketapiuser.dto.UserDTO;
import org.shyu.marketapiuser.dto.UserRegisterDTO;
import org.shyu.marketcommon.result.Result;
import org.shyu.marketserviceuser.service.UserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * User Auth Controller
 * Provides authentication-related interfaces for Auth Service
 *
 * 注意：Gateway 配置了 StripPrefix=1，会移除 /api 前缀
 * 所以这里的 RequestMapping 是 /user/auth，而不是 /api/user/auth
 *
 * 完整路径示例：
 * - 前端请求: /api/user/auth/register
 * - Gateway 转发: /user/auth/register (StripPrefix 后)
 * - Controller 处理: @RequestMapping("/user/auth") + @PostMapping("/register")
 */
@Slf4j
@RestController
@RequestMapping("/user/auth")
@RequiredArgsConstructor
public class UserAuthController {

    private final UserService userService;

    /**
     * User Registration
     * Called by Auth Service
     */
    @PostMapping("/register")
    public Result<Long> register(@Validated @RequestBody UserRegisterDTO registerDTO) {
        log.info("Registration request: username={}", registerDTO.getUsername());

        try {
            Long userId = userService.register(registerDTO);
            return Result.success("Registration successful", userId);
        } catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * Validate Login Credentials
     * Called by Auth Service
     * @param username username
     * @param password plain password
     * @return user info if valid
     */
    @PostMapping("/validate-login")
    public Result<UserDTO> validateLogin(@RequestParam String username,
                                         @RequestParam String password) {
        log.info("Validate login request: username={}", username);

        try {
            UserDTO userDTO = userService.validateLogin(username, password);
            return Result.success(userDTO);
        } catch (Exception e) {
            log.error("Login validation failed: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}

