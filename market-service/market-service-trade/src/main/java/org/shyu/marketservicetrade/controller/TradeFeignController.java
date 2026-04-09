package org.shyu.marketservicetrade.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.shyu.marketapitrade.dto.OrderDTO;
import org.shyu.marketcommon.result.Result;
import org.shyu.marketservicetrade.entity.Order;
import org.shyu.marketservicetrade.service.OrderService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 交易服务对外 Feign 接口
 * 供其他微服务调用（不经过 Gateway，直接服务间调用）
 */
@Api(tags = "交易Feign接口")
@RestController
@RequestMapping("/feign/trade")
@RequiredArgsConstructor
public class TradeFeignController {

    private final OrderService orderService;

    @ApiOperation("根据ID查询订单")
    @GetMapping("/order/{id}")
    public Result<OrderDTO> getOrderById(@PathVariable("id") Long id) {
        Order order = orderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        OrderDTO dto = new OrderDTO();
        BeanUtils.copyProperties(order, dto);
        dto.setAmount(order.getTotalAmount());
        return Result.success(dto);
    }

    @ApiOperation("根据订单号查询订单")
    @GetMapping("/order/no/{orderNo}")
    public Result<OrderDTO> getOrderByNo(@PathVariable("orderNo") String orderNo) {
        Order order = orderService.getByOrderNo(orderNo);
        if (order == null) {
            return Result.error("订单不存在");
        }
        OrderDTO dto = new OrderDTO();
        BeanUtils.copyProperties(order, dto);
        dto.setAmount(order.getTotalAmount());
        return Result.success(dto);
    }
}

