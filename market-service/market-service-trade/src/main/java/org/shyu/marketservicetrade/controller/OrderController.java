package org.shyu.marketservicetrade.controller;

import cn.dev33.satoken.stp.StpUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.shyu.marketcommon.model.PageResult;
import org.shyu.marketcommon.result.Result;
import org.shyu.marketservicetrade.dto.CreateOrderRequest;
import org.shyu.marketservicetrade.dto.OrderQueryRequest;
import org.shyu.marketservicetrade.service.OrderService;
import org.shyu.marketservicetrade.vo.OrderVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器
 */
@Api(tags = "订单管理")
@RestController
@RequestMapping("/trade/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @ApiOperation("创建订单")
    @PostMapping("/create")
    public Result<OrderVO> createOrder(@RequestBody @Validated CreateOrderRequest request) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        OrderVO orderVO = orderService.createOrder(request, userId);
        return Result.success(orderVO);
    }

    @ApiOperation("取消订单")
    @PostMapping("/cancel/{orderId}")
    public Result<Void> cancelOrder(@PathVariable Long orderId) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        orderService.cancelOrder(orderId, userId);
        return Result.success();
    }

    @ApiOperation("支付订单")
    @PostMapping("/pay/{orderId}")
    public Result<Void> payOrder(@PathVariable Long orderId) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        orderService.payOrder(orderId, userId);
        return Result.success();
    }

    @ApiOperation("确认发货")
    @PostMapping("/ship/{orderId}")
    public Result<Void> shipOrder(@PathVariable Long orderId) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        orderService.shipOrder(orderId, userId);
        return Result.success();
    }

    @ApiOperation("确认收货")
    @PostMapping("/receive/{orderId}")
    public Result<Void> receiveOrder(@PathVariable Long orderId) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        orderService.receiveOrder(orderId, userId);
        return Result.success();
    }

    @ApiOperation("获取订单详情")
    @GetMapping("/{orderId}")
    public Result<OrderVO> getOrderDetail(@PathVariable Long orderId) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        OrderVO orderVO = orderService.getOrderDetail(orderId, userId);
        return Result.success(orderVO);
    }

    @ApiOperation("我的订单列表(买家)")
    @GetMapping("/my")
    public Result<PageResult<OrderVO>> getMyOrders(OrderQueryRequest request) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        PageResult<OrderVO> pageResult = orderService.getMyOrders(request, userId);
        return Result.success(pageResult);
    }

    @ApiOperation("我的销售订单列表(卖家)")
    @GetMapping("/sales")
    public Result<PageResult<OrderVO>> getMySalesOrders(OrderQueryRequest request) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        PageResult<OrderVO> pageResult = orderService.getMySalesOrders(request, userId);
        return Result.success(pageResult);
    }
}

