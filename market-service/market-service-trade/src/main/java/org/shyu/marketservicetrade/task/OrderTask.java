package org.shyu.marketservicetrade.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shyu.marketapiproduct.feign.ProductFeignClient;
import org.shyu.marketservicetrade.entity.Order;
import org.shyu.marketservicetrade.enums.OrderStatus;
import org.shyu.marketservicetrade.service.OrderLogService;
import org.shyu.marketservicetrade.service.OrderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTask {

    private final OrderService orderService;
    private final OrderLogService orderLogService;
    private final ProductFeignClient productFeignClient;

    /**
     * 自动取消超时未支付的订单
     * 每5分钟执行一次，取消30分钟未支付的订单
     */
    @Scheduled(fixedRate = 5 * 60 * 1000) // 5分钟
    @Transactional(rollbackFor = Exception.class)
    public void cancelTimeoutOrders() {
        try {
            // 查询30分钟前创建且仍为待支付状态的订单
            LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(30);

            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getStatus, OrderStatus.WAIT_PAY.getCode())
                   .lt(Order::getCreateTime, timeoutTime);

            List<Order> timeoutOrders = orderService.list(wrapper);

            if (timeoutOrders.isEmpty()) {
                return;
            }

            log.info("发现 {} 个超时未支付订单，开始自动取消", timeoutOrders.size());

            for (Order order : timeoutOrders) {
                try {
                    // 更新订单状态为已取消
                    order.setStatus(OrderStatus.CANCELLED.getCode());
                    orderService.updateById(order);

                    // 记录日志
                    orderLogService.saveLog(order.getId(), OrderStatus.CANCELLED.getCode(),
                            "SYSTEM", "系统自动取消超时订单");

                    // 恢复商品状态为已发布(status=1)，商品重新上架可被购买
                    productFeignClient.updateProductStatus(order.getProductId(), 1);

                    log.info("自动取消超时订单成功，orderNo: {}, productId: {}",
                            order.getOrderNo(), order.getProductId());

                } catch (Exception e) {
                    log.error("自动取消超时订单失败，orderNo: {}, productId: {}",
                            order.getOrderNo(), order.getProductId(), e);
                }
            }

        } catch (Exception e) {
            log.error("执行自动取消超时订单任务失败", e);
        }
    }

    /**
     * 自动确认收货
     * 每小时执行一次，自动确认发货后7天未确认收货的订单
     */
    @Scheduled(fixedRate = 60 * 60 * 1000) // 1小时
    @Transactional(rollbackFor = Exception.class)
    public void autoConfirmReceive() {
        try {
            // 查询7天前发货且仍为已发货状态的订单
            LocalDateTime autoConfirmTime = LocalDateTime.now().minusDays(7);

            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getStatus, OrderStatus.SHIPPED.getCode())
                   .lt(Order::getUpdateTime, autoConfirmTime);

            List<Order> autoConfirmOrders = orderService.list(wrapper);

            if (autoConfirmOrders.isEmpty()) {
                return;
            }

            log.info("发现 {} 个超时未确认收货订单，开始自动确认", autoConfirmOrders.size());

            for (Order order : autoConfirmOrders) {
                try {
                    // 更新订单状态为已完成
                    order.setStatus(OrderStatus.COMPLETED.getCode());
                    orderService.updateById(order);

                    // 记录日志
                    orderLogService.saveLog(order.getId(), OrderStatus.COMPLETED.getCode(),
                            "SYSTEM", "系统自动确认收货（发货7天后）");

                    log.info("自动确认收货成功，orderNo: {}", order.getOrderNo());

                } catch (Exception e) {
                    log.error("自动确认收货失败，orderNo: {}", order.getOrderNo(), e);
                }
            }

        } catch (Exception e) {
            log.error("执行自动确认收货任务失败", e);
        }
    }
}