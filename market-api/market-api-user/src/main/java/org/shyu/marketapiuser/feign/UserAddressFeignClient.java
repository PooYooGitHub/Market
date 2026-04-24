package org.shyu.marketapiuser.feign;

import org.shyu.marketapiuser.dto.UserAddressDTO;
import org.shyu.marketcommon.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 用户地址 Feign Client（内部调用）
 */
@FeignClient(name = "market-service-user", contextId = "userAddressFeignClient", path = "/feign/user/address")
public interface UserAddressFeignClient {

    /**
     * 按用户ID+地址ID查询地址（仅返回该用户名下地址）
     */
    @GetMapping("/by-id")
    Result<UserAddressDTO> getByUserIdAndAddressId(@RequestParam("userId") Long userId,
                                                   @RequestParam("addressId") Long addressId);

    /**
     * 查询用户默认地址
     */
    @GetMapping("/default")
    Result<UserAddressDTO> getDefaultByUserId(@RequestParam("userId") Long userId);
}
