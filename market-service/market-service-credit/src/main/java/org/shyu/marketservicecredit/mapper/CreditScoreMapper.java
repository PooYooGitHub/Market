package org.shyu.marketservicecredit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.shyu.marketservicecredit.entity.CreditScore;

@Mapper
public interface CreditScoreMapper extends BaseMapper<CreditScore> {

    @Select("SELECT * FROM t_credit_score WHERE user_id = #{userId}")
    CreditScore getByUserId(Long userId);

    @Select("SELECT * FROM t_credit_score WHERE user_id = #{userId} FOR UPDATE")
    CreditScore getByUserIdForUpdate(Long userId);

    @Insert("INSERT INTO t_credit_score (user_id, score, level, badge_code, badge_name, badge_color, badge_desc, high_trust, valid_trade_count) " +
            "VALUES (#{userId}, 550, '成长中', 'ROOKIE', '新手认证', '#C0C4CC', '交易次数较少，建议先小额交易', 0, 0) " +
            "ON DUPLICATE KEY UPDATE user_id = user_id")
    Integer initUserCredit(Long userId);
}
