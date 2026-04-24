package org.shyu.marketserviceuser.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.shyu.marketapiuser.dto.UserAddressDTO;
import org.shyu.marketserviceuser.dto.UserAddressSaveRequest;
import org.shyu.marketserviceuser.dto.UserAddressUpdateRequest;
import org.shyu.marketserviceuser.entity.UserAddressEntity;

import java.util.List;

/**
 * 用户收货地址服务
 */
public interface UserAddressService extends IService<UserAddressEntity> {

    Long createAddress(Long userId, UserAddressSaveRequest request);

    List<UserAddressDTO> listByUserId(Long userId);

    UserAddressDTO getDetail(Long userId, Long addressId);

    void updateAddress(Long userId, Long addressId, UserAddressUpdateRequest request);

    void deleteAddress(Long userId, Long addressId);

    void setDefaultAddress(Long userId, Long addressId);

    UserAddressDTO getDefaultAddress(Long userId);

    UserAddressDTO getByUserIdAndAddressId(Long userId, Long addressId);
}
