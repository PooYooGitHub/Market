package org.shyu.marketservicecredit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.shyu.marketservicecredit.entity.CreditLog;

@Mapper
public interface CreditLogMapper extends BaseMapper<CreditLog> {

    @Select("SELECT COUNT(1) FROM t_credit_log WHERE event_key = #{eventKey}")
    int countByEventKey(String eventKey);
}
