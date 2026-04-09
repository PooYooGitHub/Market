package org.shyu.marketservicecredit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.shyu.marketservicecredit.entity.CreditScore;
import org.shyu.marketapicredit.vo.CreditVO;

/**
 * 信用分服务接口
 *
 * @author Market Team
 * @since 2026-04-01
 */
public interface CreditScoreService extends IService<CreditScore> {

    /**
     * 获取用户信用信息
     *
     * @param userId 用户ID
     * @return 信用信息
     */
    CreditVO getUserCredit(Long userId);

    /**
     * 初始化用户信用分
     *
     * @param userId 用户ID
     */
    void initUserCredit(Long userId);

    /**
     * 更新用户信用分
     *
     * @param userId    用户ID
     * @param scoreChange 分数变化
     */
    void updateUserCredit(Long userId, Integer scoreChange);

    /**
     * 重新计算用户信用分
     * 基于用户的所有评价重新计算
     *
     * @param userId 用户ID
     */
    void recalculateUserCredit(Long userId);
}