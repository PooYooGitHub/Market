package org.shyu.marketserviceuser.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.shyu.marketapiuser.dto.UserAddressDTO;
import org.shyu.marketcommon.exception.BusinessException;
import org.shyu.marketserviceuser.dto.UserAddressSaveRequest;
import org.shyu.marketserviceuser.dto.UserAddressUpdateRequest;
import org.shyu.marketserviceuser.entity.UserAddressEntity;
import org.shyu.marketserviceuser.mapper.UserAddressMapper;
import org.shyu.marketserviceuser.service.UserAddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户收货地址服务实现
 */
@Slf4j
@Service
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddressEntity> implements UserAddressService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAddress(Long userId, UserAddressSaveRequest request) {
        long count = lambdaQuery().eq(UserAddressEntity::getUserId, userId).count();
        boolean shouldDefault = count == 0 || Boolean.TRUE.equals(request.getIsDefault());

        if (shouldDefault) {
            clearDefaultAddress(userId);
        }

        UserAddressEntity entity = new UserAddressEntity();
        entity.setUserId(userId);
        applySaveRequest(entity, request);
        entity.setIsDefault(shouldDefault);

        save(entity);
        return entity.getId();
    }

    @Override
    public List<UserAddressDTO> listByUserId(Long userId) {
        List<UserAddressEntity> list = lambdaQuery()
                .eq(UserAddressEntity::getUserId, userId)
                .orderByDesc(UserAddressEntity::getIsDefault)
                .orderByDesc(UserAddressEntity::getUpdateTime)
                .list();
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public UserAddressDTO getDetail(Long userId, Long addressId) {
        UserAddressEntity entity = getOwnedAddressOrThrow(userId, addressId);
        return toDTO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAddress(Long userId, Long addressId, UserAddressUpdateRequest request) {
        UserAddressEntity entity = getOwnedAddressOrThrow(userId, addressId);
        applyUpdateRequest(entity, request);

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultAddress(userId);
            entity.setIsDefault(true);
        }

        updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAddress(Long userId, Long addressId) {
        UserAddressEntity entity = getOwnedAddressOrThrow(userId, addressId);
        boolean wasDefault = Boolean.TRUE.equals(entity.getIsDefault());

        removeById(addressId);

        // 删除默认地址后，自动把最新一条地址设置为默认地址（若还有地址）
        if (wasDefault) {
            UserAddressEntity candidate = lambdaQuery()
                    .eq(UserAddressEntity::getUserId, userId)
                    .orderByDesc(UserAddressEntity::getUpdateTime)
                    .last("limit 1")
                    .one();
            if (candidate != null) {
                clearDefaultAddress(userId);
                candidate.setIsDefault(true);
                updateById(candidate);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultAddress(Long userId, Long addressId) {
        UserAddressEntity entity = getOwnedAddressOrThrow(userId, addressId);
        clearDefaultAddress(userId);
        entity.setIsDefault(true);
        updateById(entity);
    }

    @Override
    public UserAddressDTO getDefaultAddress(Long userId) {
        UserAddressEntity entity = lambdaQuery()
                .eq(UserAddressEntity::getUserId, userId)
                .eq(UserAddressEntity::getIsDefault, true)
                .orderByDesc(UserAddressEntity::getUpdateTime)
                .last("limit 1")
                .one();
        return entity == null ? null : toDTO(entity);
    }

    @Override
    public UserAddressDTO getByUserIdAndAddressId(Long userId, Long addressId) {
        UserAddressEntity entity = lambdaQuery()
                .eq(UserAddressEntity::getUserId, userId)
                .eq(UserAddressEntity::getId, addressId)
                .last("limit 1")
                .one();
        return entity == null ? null : toDTO(entity);
    }

    private UserAddressEntity getOwnedAddressOrThrow(Long userId, Long addressId) {
        UserAddressEntity entity = lambdaQuery()
                .eq(UserAddressEntity::getUserId, userId)
                .eq(UserAddressEntity::getId, addressId)
                .last("limit 1")
                .one();
        if (entity == null) {
            throw new BusinessException("收货地址不存在");
        }
        return entity;
    }

    private void clearDefaultAddress(Long userId) {
        lambdaUpdate()
                .eq(UserAddressEntity::getUserId, userId)
                .eq(UserAddressEntity::getIsDefault, true)
                .set(UserAddressEntity::getIsDefault, false)
                .update();
    }

    private void applySaveRequest(UserAddressEntity entity, UserAddressSaveRequest request) {
        entity.setReceiverName(normalize(request.getReceiverName()));
        entity.setReceiverPhone(normalize(request.getReceiverPhone()));
        entity.setProvince(normalize(request.getProvince()));
        entity.setCity(normalize(request.getCity()));
        entity.setDistrict(normalize(request.getDistrict()));
        entity.setDetailAddress(normalize(request.getDetailAddress()));
        entity.setPostalCode(normalize(request.getPostalCode()));
    }

    private void applyUpdateRequest(UserAddressEntity entity, UserAddressUpdateRequest request) {
        entity.setReceiverName(normalize(request.getReceiverName()));
        entity.setReceiverPhone(normalize(request.getReceiverPhone()));
        entity.setProvince(normalize(request.getProvince()));
        entity.setCity(normalize(request.getCity()));
        entity.setDistrict(normalize(request.getDistrict()));
        entity.setDetailAddress(normalize(request.getDetailAddress()));
        entity.setPostalCode(normalize(request.getPostalCode()));
    }

    private UserAddressDTO toDTO(UserAddressEntity entity) {
        UserAddressDTO dto = new UserAddressDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setReceiverName(entity.getReceiverName());
        dto.setReceiverPhone(entity.getReceiverPhone());
        dto.setProvince(entity.getProvince());
        dto.setCity(entity.getCity());
        dto.setDistrict(entity.getDistrict());
        dto.setDetailAddress(entity.getDetailAddress());
        dto.setPostalCode(entity.getPostalCode());
        dto.setIsDefault(entity.getIsDefault());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateTime(entity.getUpdateTime());
        return dto;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
