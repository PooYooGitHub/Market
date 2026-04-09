package org.shyu.marketserviceuser.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.shyu.marketapiuser.dto.UserDTO;
import org.shyu.marketapiuser.dto.UserLoginDTO;
import org.shyu.marketapiuser.dto.UserRegisterDTO;
import org.shyu.marketapiuser.entity.User;
import org.shyu.marketapiuser.vo.LoginVO;
import org.shyu.marketapiuser.vo.UserVO;
import org.shyu.marketserviceuser.entity.UserEntity;
import org.shyu.marketserviceuser.vo.UserStatisticsVO;

/**
 * User Service Interface
 */
public interface UserService extends IService<UserEntity> {

    /**
     * User Registration (for Auth Service)
     * @param registerDTO registration info
     * @return userId
     */
    Long register(UserRegisterDTO registerDTO);


    /**
     * Validate Login Credentials (for Auth Service)
     * @param username username
     * @param password plain password
     * @return user info if valid
     */
    UserDTO validateLogin(String username, String password);

    /**
     * User Login (legacy)
     * @param loginDTO login info
     * @return login result with token
     */
    LoginVO login(UserLoginDTO loginDTO);

    // ...existing code...

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

    /**
     * 获取用户的角色列表
     * @param userId 用户ID
     * @return 角色代码列表
     */
    java.util.List<String> getUserRoles(Long userId);
}

