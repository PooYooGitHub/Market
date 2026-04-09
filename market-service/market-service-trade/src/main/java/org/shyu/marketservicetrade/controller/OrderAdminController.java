package org.shyu.marketservicetrade.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.shyu.marketcommon.result.Result;
import org.shyu.marketservicetrade.dto.OrderQueryRequest;
import org.shyu.marketservicetrade.dto.OrderRefundRequest;
import org.shyu.marketservicetrade.service.OrderService;
import org.shyu.marketservicetrade.vo.OrderDetailVO;
import org.shyu.marketservicetrade.vo.OrderListVO;
import org.shyu.marketservicetrade.vo.OrderStatisticsVO;
import org.shyu.marketservicetrade.vo.TransactionAnalysisVO;

import java.util.List;
import java.util.Map;

/**
 * 订单管理控制器（管理员专用）
 * 提供订单的管理、统计、分析等功能
 *
 * @author shyu
 * @since 2026-04-05
 */
@Api(tags = "订单管理（管理员）")
@Slf4j
@RestController
@RequestMapping("/order/admin")
@RequiredArgsConstructor
public class OrderAdminController {

    private final OrderService orderService;

    /**
     * 分页查询所有订单（管理员视图）
     */
    @ApiOperation("分页查询所有订单")
    @GetMapping("/list")
    @SaCheckRole("admin")
    public Result<Page<OrderListVO>> listAllOrders(OrderQueryRequest request) {
        Page<OrderListVO> page = orderService.listAllOrdersForAdmin(request);
        return Result.success(page);
    }

    /**
     * 根据ID查询订单详情（管理员视图）
     */
    @ApiOperation("查询订单详情")
    @GetMapping("/{id}")
    @SaCheckRole("admin")
    public Result<OrderDetailVO> getOrderById(@PathVariable Long id) {
        OrderDetailVO order = orderService.getOrderDetailForAdmin(id);
        return Result.success(order);
    }

    /**
     * 根据订单号查询订单
     */
    @ApiOperation("根据订单号查询订单")
    @GetMapping("/by-no/{orderNo}")
    @SaCheckRole("admin")
    public Result<OrderDetailVO> getOrderByNo(@PathVariable String orderNo) {
        OrderDetailVO order = orderService.getOrderByNoForAdmin(orderNo);
        return Result.success(order);
    }

    /**
     * 取消订单（管理员操作）
     */
    @ApiOperation("取消订单")
    @PutMapping("/{id}/cancel")
    @SaCheckRole("admin")
    public Result<String> cancelOrder(@PathVariable Long id,
                                     @RequestParam(required = false) String reason) {
        orderService.cancelOrderByAdmin(id, reason);
        return Result.success("订单已取消");
    }

    /**
     * 批量取消订单
     */
    @ApiOperation("批量取消订单")
    @PutMapping("/batch-cancel")
    @SaCheckRole("admin")
    public Result<String> batchCancelOrders(@RequestBody List<Long> ids,
                                           @RequestParam(required = false) String reason) {
        orderService.batchCancelOrders(ids, reason);
        return Result.success("批量取消完成");
    }

    /**
     * 强制完成订单
     */
    @ApiOperation("强制完成订单")
    @PutMapping("/{id}/force-complete")
    @SaCheckRole("admin")
    public Result<String> forceCompleteOrder(@PathVariable Long id,
                                            @RequestParam(required = false) String reason) {
        orderService.forceCompleteOrder(id, reason);
        return Result.success("订单已强制完成");
    }

    /**
     * 处理退款申请
     */
    @ApiOperation("处理退款申请")
    @PostMapping("/{id}/refund")
    @SaCheckRole("admin")
    public Result<String> processRefund(@PathVariable Long id,
                                       @RequestBody OrderRefundRequest request) {
        orderService.processRefund(id, request);
        return Result.success("退款处理完成");
    }

    /**
     * 获取订单统计数据
     */
    @ApiOperation("获取订单统计数据")
    @GetMapping("/statistics")
    @SaCheckRole("admin")
    public Result<OrderStatisticsVO> getOrderStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        OrderStatisticsVO statistics = orderService.getOrderStatistics(startDate, endDate);
        return Result.success(statistics);
    }

    /**
     * 获取交易分析数据
     */
    @ApiOperation("获取交易分析数据")
    @GetMapping("/analysis")
    @SaCheckRole("admin")
    public Result<TransactionAnalysisVO> getTransactionAnalysis(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "day") String groupBy) {
        TransactionAnalysisVO analysis = orderService.getTransactionAnalysis(startDate, endDate, groupBy);
        return Result.success(analysis);
    }

    /**
     * 获取订单状态统计
     */
    @ApiOperation("获取订单状态统计")
    @GetMapping("/status-statistics")
    @SaCheckRole("admin")
    public Result<Map<String, Long>> getOrderStatusStatistics() {
        Map<String, Long> statistics = orderService.getOrderStatusStatistics();
        return Result.success(statistics);
    }

    /**
     * 获取热门商品销售排行
     */
    @ApiOperation("获取热门商品销售排行")
    @GetMapping("/hot-sales")
    @SaCheckRole("admin")
    public Result<List<Map<String, Object>>> getHotSalesProducts(
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<Map<String, Object>> hotSales = orderService.getHotSalesProducts(limit, startDate, endDate);
        return Result.success(hotSales);
    }

    /**
     * 获取用户购买力排行
     */
    @ApiOperation("获取用户购买力排行")
    @GetMapping("/buyer-ranking")
    @SaCheckRole("admin")
    public Result<List<Map<String, Object>>> getBuyerRanking(
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<Map<String, Object>> ranking = orderService.getBuyerRanking(limit, startDate, endDate);
        return Result.success(ranking);
    }

    /**
     * 获取卖家销售排行
     */
    @ApiOperation("获取卖家销售排行")
    @GetMapping("/seller-ranking")
    @SaCheckRole("admin")
    public Result<List<Map<String, Object>>> getSellerRanking(
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<Map<String, Object>> ranking = orderService.getSellerRanking(limit, startDate, endDate);
        return Result.success(ranking);
    }

    /**
     * 导出订单数据
     */
    @ApiOperation("导出订单数据")
    @PostMapping("/export")
    @SaCheckRole("admin")
    public Result<String> exportOrders(@RequestBody OrderQueryRequest request) {
        String exportPath = orderService.exportOrders(request);
        return Result.success("导出成功", exportPath);
    }
}