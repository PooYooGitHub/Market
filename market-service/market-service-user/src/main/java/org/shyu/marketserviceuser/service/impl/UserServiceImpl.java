package org.shyu.marketserviceuser.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.shyu.marketapiuser.dto.UserLoginDTO;
import org.shyu.marketapiuser.dto.UserRegisterDTO;
import org.shyu.marketapiuser.entity.User;
import org.shyu.marketapiuser.vo.LoginVO;
import org.shyu.marketapiuser.vo.UserVO;
import org.shyu.marketcommon.exception.BusinessException;
import org.shyu.marketserviceuser.vo.UserStatisticsVO;
import org.shyu.marketserviceuser.entity.UserEntity;
import org.shyu.marketserviceuser.mapper.UserMapper;
import org.shyu.marketserviceuser.service.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(UserRegisterDTO registerDTO) {
        // 1. 验证密码一致性
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new BusinessException("两次密码输入不一致");
        }

        // 2. 检查用户名是否已存在
        User existUser = getByUsername(registerDTO.getUsername());
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 3. 检查手机号是否已存在
        if (registerDTO.getPhone() != null && !registerDTO.getPhone().isEmpty()) {
            User phoneUser = getByPhone(registerDTO.getPhone());
            if (phoneUser != null) {
                throw new BusinessException("手机号已被注册");
            }
        }

        // 4. 创建用户对象
        UserEntity user = new UserEntity();
        user.setUsername(registerDTO.getUsername());
        // 使用BCrypt加密密码
        user.setPassword(BCrypt.hashpw(registerDTO.getPassword()));
        user.setNickname(registerDTO.getNickname() != null ? registerDTO.getNickname() : registerDTO.getUsername());
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());
        user.setStatus(1); // 默认正常状态

        // 5. 保存到数据库
        boolean saved = save(user);
        if (!saved) {
            throw new BusinessException("注册失败，请稍后重试");
        }

        log.info("用户注册成功: username={}, id={}", user.getUsername(), user.getId());

        // 6. 转换为VO返回
        return BeanUtil.copyProperties(user, UserVO.class);
    }

    @Override
    public LoginVO login(UserLoginDTO loginDTO) {
        // 1. 查询用户
        User user = getByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 2. 验证密码
        if (!BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 3. 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被删除");
        }
        if (user.getStatus() == 2) {
            throw new BusinessException("账号已被禁用");
        }

        // 4. 生成token
        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();
        long timeout = StpUtil.getTokenTimeout();

        log.info("用户登录成功: username={}, id={}", user.getUsername(), user.getId());

        // 5. 构建返回结果
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setExpiresIn(timeout);
        loginVO.setUserInfo(BeanUtil.copyProperties(user, UserVO.class));

        return loginVO;
    }

    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getUsername, username);
        UserEntity entity = getOne(wrapper);
        return entity != null ? BeanUtil.copyProperties(entity, User.class) : null;
    }

    @Override
    public User getByPhone(String phone) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getPhone, phone);
        UserEntity entity = getOne(wrapper);
        return entity != null ? BeanUtil.copyProperties(entity, User.class) : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        // 1. 查询用户
        UserEntity user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 验证旧密码
        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }

        // 3. 更新密码
        user.setPassword(BCrypt.hashpw(newPassword));
        boolean updated = updateById(user);

        if (updated) {
            log.info("用户修改密码成功: userId={}", userId);
            // 踢下线，要求重新登录
            StpUtil.kickout(userId);
        }

        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(Long userId, String newPassword) {
        // 1. 查询用户
        UserEntity user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 更新密码
        user.setPassword(BCrypt.hashpw(newPassword));
        boolean updated = updateById(user);

        if (updated) {
            log.info("管理员重置用户密码: userId={}", userId);
        }

        return updated;
    }

    @Override
    public UserStatisticsVO getStatistics() {
        UserStatisticsVO statistics = new UserStatisticsVO();

        // 总用户数
        statistics.setTotalCount(count());

        // 正常用户数（status=1）
        LambdaQueryWrapper<UserEntity> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.eq(UserEntity::getStatus, 1);
        statistics.setActiveCount(count(activeWrapper));

        // 禁用用户数（status=0）
        LambdaQueryWrapper<UserEntity> disabledWrapper = new LambdaQueryWrapper<>();
        disabledWrapper.eq(UserEntity::getStatus, 0);
        statistics.setDisabledCount(count(disabledWrapper));

        // 注：表结构中没有删除标记，所以删除数设为0
        statistics.setDeletedCount(0L);

        // 今日注册数
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        LambdaQueryWrapper<UserEntity> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.between(UserEntity::getCreateTime, todayStart, todayEnd);
        statistics.setTodayRegister(count(todayWrapper));

        return statistics;
    }
}

