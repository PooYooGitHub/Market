package org.shyu.marketservicetrade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.shyu.marketservicetrade.entity.OrderLog;

import java.util.List;

/**
 * 订单日志服务接口
 */
public interface OrderLogService extends IService<OrderLog> {

    /**
     * 记录订单日志
     *
     * @param orderId  订单ID
     * @param status   状态
     * @param operator 操作人
     * @param remark   备注
     */
    void saveLog(Long orderId, Integer status, String operator, String remark);

    /**
     * 获取订单日志列表
     *
     * @param orderId 订单ID
     * @return 订单日志列表
     */
    List<OrderLog> getOrderLogs(Long orderId);
}
