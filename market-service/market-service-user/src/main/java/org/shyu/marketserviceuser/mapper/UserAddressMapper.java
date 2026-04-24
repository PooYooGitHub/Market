package org.shyu.marketserviceuser.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.shyu.marketserviceuser.entity.UserAddressEntity;

/**
 * 用户收货地址 Mapper
 */
@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddressEntity> {
}
