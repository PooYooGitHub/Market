package org.shyu.marketservicecredit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.shyu.marketservicecredit.entity.CreditScore;

/**
 * 信用分 Mapper 接口
 *
 * @author Market Team
 * @since 2026-04-01
 */
@Mapper
public interface CreditScoreMapper extends BaseMapper<CreditScore> {

    /**
     * 根据用户ID获取信用分
     *
     * @param userId 用户ID
     * @return 信用分记录
     */
    @Select("SELECT * FROM t_credit_score WHERE user_id = #{userId}")
    CreditScore getByUserId(Long userId);

    /**
     * 初始化用户信用分
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    @Select("INSERT INTO t_credit_score (user_id, score, level) VALUES (#{userId}, 100, '一般') " +
            "ON DUPLICATE KEY UPDATE user_id = user_id")
    Integer initUserCredit(Long userId);
}