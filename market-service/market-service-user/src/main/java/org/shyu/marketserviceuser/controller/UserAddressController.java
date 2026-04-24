package org.shyu.marketserviceuser.controller;

import cn.dev33.satoken.stp.StpUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.shyu.marketapiuser.dto.UserAddressDTO;
import org.shyu.marketcommon.result.Result;
import org.shyu.marketserviceuser.dto.UserAddressSaveRequest;
import org.shyu.marketserviceuser.dto.UserAddressUpdateRequest;
import org.shyu.marketserviceuser.service.UserAddressService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户收货地址控制器
 */
@Api(tags = "用户收货地址")
@RestController
@RequestMapping("/user/address")
@RequiredArgsConstructor
public class UserAddressController {

    private final UserAddressService userAddressService;

    @ApiOperation("新增收货地址")
    @PostMapping
    public Result<Long> createAddress(@Validated @RequestBody UserAddressSaveRequest request) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        Long id = userAddressService.createAddress(userId, request);
        return Result.success("新增成功", id);
    }

    @ApiOperation("获取当前用户地址列表")
    @GetMapping("/list")
    public Result<List<UserAddressDTO>> listAddresses() {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(userAddressService.listByUserId(userId));
    }

    @ApiOperation("获取单个地址详情")
    @GetMapping("/{addressId}")
    public Result<UserAddressDTO> getAddress(@PathVariable Long addressId) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(userAddressService.getDetail(userId, addressId));
    }

    @ApiOperation("更新地址")
    @PutMapping("/{addressId}")
    public Result<Void> updateAddress(@PathVariable Long addressId,
                                      @Validated @RequestBody UserAddressUpdateRequest request) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        userAddressService.updateAddress(userId, addressId, request);
        return Result.success();
    }

    @ApiOperation("删除地址")
    @DeleteMapping("/{addressId}")
    public Result<Void> deleteAddress(@PathVariable Long addressId) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        userAddressService.deleteAddress(userId, addressId);
        return Result.success();
    }

    @ApiOperation("设置默认地址")
    @PutMapping("/{addressId}/default")
    public Result<Void> setDefaultAddress(@PathVariable Long addressId) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        userAddressService.setDefaultAddress(userId, addressId);
        return Result.success();
    }

    @ApiOperation("获取默认地址")
    @GetMapping("/default")
    public Result<UserAddressDTO> getDefaultAddress() {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(userAddressService.getDefaultAddress(userId));
    }
}
