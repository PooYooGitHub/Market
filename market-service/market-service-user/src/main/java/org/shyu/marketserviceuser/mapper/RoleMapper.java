package org.shyu.marketserviceuser.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.shyu.marketserviceuser.entity.RoleEntity;

/**
 * 角色数据访问层
 */
@Mapper
public interface RoleMapper extends BaseMapper<RoleEntity> {
}