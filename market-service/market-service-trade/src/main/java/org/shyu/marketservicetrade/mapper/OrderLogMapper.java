package org.shyu.marketservicetrade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.shyu.marketservicetrade.entity.OrderLog;

/**
 * 订单日志Mapper
 */
@Mapper
public interface OrderLogMapper extends BaseMapper<OrderLog> {
}

