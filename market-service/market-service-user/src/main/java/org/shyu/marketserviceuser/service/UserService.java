package org.shyu.marketserviceuser.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.shyu.marketapiuser.dto.UserLoginDTO;
import org.shyu.marketapiuser.dto.UserRegisterDTO;
import org.shyu.marketapiuser.entity.User;
import org.shyu.marketapiuser.vo.LoginVO;
import org.shyu.marketapiuser.vo.UserVO;
import org.shyu.marketserviceuser.entity.UserEntity;
import org.shyu.marketserviceuser.vo.UserStatisticsVO;

/**
 * 用户服务接口
 */
public interface UserService extends IService<UserEntity> {

    /**
     * 用户注册
     * @param registerDTO 注册信息
     * @return 用户信息
     */
    UserVO register(UserRegisterDTO registerDTO);

    /**
     * 用户登录
     * @param loginDTO 登录信息
     * @return 登录结果（包含token）
     */
    LoginVO login(UserLoginDTO loginDTO);

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    User getByUsername(String username);

    /**
     * 根据手机号查询用户
     * @param phone 手机号
     * @return 用户信息
     */
    User getByPhone(String phone);

    /**
     * 修改密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 重置密码（管理员）
     * @param userId 用户ID
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean resetPassword(Long userId, String newPassword);

    /**
     * 获取用户统计数据
     * @return 统计信息
     */
    UserStatisticsVO getStatistics();
}

