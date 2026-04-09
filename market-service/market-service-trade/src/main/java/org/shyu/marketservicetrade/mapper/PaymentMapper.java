package org.shyu.marketservicetrade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.shyu.marketservicetrade.entity.Payment;

/**
 * 支付记录Mapper
 */
@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {
}