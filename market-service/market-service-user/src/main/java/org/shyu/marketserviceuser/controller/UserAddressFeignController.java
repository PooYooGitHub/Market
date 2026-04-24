package org.shyu.marketserviceuser.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.shyu.marketapiuser.dto.UserAddressDTO;
import org.shyu.marketcommon.result.Result;
import org.shyu.marketserviceuser.service.UserAddressService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户地址 Feign 内部接口
 */
@Api(tags = "用户地址Feign接口")
@RestController
@RequestMapping("/feign/user/address")
@RequiredArgsConstructor
public class UserAddressFeignController {

    private final UserAddressService userAddressService;

    @ApiOperation("按用户ID+地址ID查询地址")
    @GetMapping("/by-id")
    public Result<UserAddressDTO> getByUserIdAndAddressId(@RequestParam("userId") Long userId,
                                                          @RequestParam("addressId") Long addressId) {
        return Result.success(userAddressService.getByUserIdAndAddressId(userId, addressId));
    }

    @ApiOperation("查询用户默认地址")
    @GetMapping("/default")
    public Result<UserAddressDTO> getDefaultByUserId(@RequestParam("userId") Long userId) {
        return Result.success(userAddressService.getDefaultAddress(userId));
    }
}
