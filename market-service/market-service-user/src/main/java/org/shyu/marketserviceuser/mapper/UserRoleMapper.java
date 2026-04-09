package org.shyu.marketserviceuser.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.shyu.marketserviceuser.entity.UserRoleEntity;

import java.util.List;

/**
 * 用户角色关系数据访问层
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRoleEntity> {

    /**
     * 根据用户ID查询用户角色信息
     * @param userId 用户ID
     * @return 角色代码列表
     */
    @Select("SELECT r.role_code FROM t_user_role ur " +
            "JOIN t_role r ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId}")
    List<String> getUserRoleCodes(Long userId);

    /**
     * 根据用户ID查询用户角色详细信息
     * @param userId 用户ID
     * @return 用户角色信息
     */
    @Select("SELECT ur.id, ur.user_id, ur.role_id, r.role_name, r.role_code, r.description " +
            "FROM t_user_role ur " +
            "JOIN t_role r ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId}")
    List<UserRoleEntity> getUserRoleDetails(Long userId);
}