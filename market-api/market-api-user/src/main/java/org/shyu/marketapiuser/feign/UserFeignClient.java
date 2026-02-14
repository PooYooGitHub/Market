package org.shyu.marketapiuser.feign;

import org.shyu.marketapiuser.dto.UserDTO;
import org.shyu.marketapiuser.dto.UserRegisterDTO;
import org.shyu.marketcommon.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * User Service Feign Client
 * Used by Auth Service to call User Service
 */
@FeignClient(name = "market-service-user", path = "/api/user")
public interface UserFeignClient {

    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    Result<UserDTO> getUserById(@PathVariable("id") Long id);

    /**
     * Get user by username
     */
    @GetMapping("/username")
    Result<UserDTO> getUserByUsername(@RequestParam("username") String username);

    /**
     * Get user by phone
     */
    @GetMapping("/phone")
    Result<UserDTO> getUserByPhone(@RequestParam("phone") String phone);

    /**
     * User registration (called by Auth Service)
     */
    @PostMapping("/auth/register")
    Result<Long> register(@RequestBody UserRegisterDTO registerDTO);

    /**
     * Validate login credentials (called by Auth Service)
     * @param username username
     * @param password password (plain text, will be validated in User Service)
     * @return user info if valid
     */
    @PostMapping("/auth/validate-login")
    Result<UserDTO> validateLogin(@RequestParam("username") String username,
                                   @RequestParam("password") String password);
}
