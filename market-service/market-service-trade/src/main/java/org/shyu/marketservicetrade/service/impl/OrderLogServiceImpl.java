package org.shyu.marketservicetrade.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.shyu.marketservicetrade.entity.OrderLog;
import org.shyu.marketservicetrade.mapper.OrderLogMapper;
import org.shyu.marketservicetrade.service.OrderLogService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单日志服务实现
 */
@Slf4j
@Service
public class OrderLogServiceImpl extends ServiceImpl<OrderLogMapper, OrderLog> implements OrderLogService {

    @Override
    public void saveLog(Long orderId, Integer status, String operator, String remark) {
        OrderLog orderLog = new OrderLog();
        orderLog.setOrderId(orderId);
        orderLog.setStatus(status);
        orderLog.setOperator(operator);
        orderLog.setRemark(remark);
        save(orderLog);

        log.info("订单日志记录成功: orderId={}, status={}, operator={}, remark={}",
                orderId, status, operator, remark);
    }

    @Override
    public List<OrderLog> getOrderLogs(Long orderId) {
        return lambdaQuery()
                .eq(OrderLog::getOrderId, orderId)
                .orderByAsc(OrderLog::getCreateTime)
                .list();
    }
}
