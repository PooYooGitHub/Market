package org.shyu.marketservicetrade.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shyu.marketcommon.model.PageQuery;
import org.shyu.marketcommon.model.R;
import org.shyu.marketservicetrade.dto.CreatePaymentRequest;
import org.shyu.marketservicetrade.dto.PaymentQueryDTO;
import org.shyu.marketservicetrade.dto.PaymentSimulateRequest;
import org.shyu.marketservicetrade.dto.RefundRequest;
import org.shyu.marketservicetrade.service.PaymentService;
import org.shyu.marketservicetrade.vo.PaymentVO;
import org.shyu.marketservicetrade.vo.UserBalanceVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 支付控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Validated
@Api(tags = "支付管理")
@SaCheckLogin
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    @ApiOperation("创建支付")
    public R<PaymentVO> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        PaymentVO result = paymentService.createPayment(request, userId);
        return R.success(result);
    }

    @PostMapping("/pay/{paymentId}")
    @ApiOperation("执行支付")
    public R<PaymentVO> executePayment(@PathVariable Long paymentId) {
        Long userId = StpUtil.getLoginIdAsLong();
        PaymentVO result = paymentService.executePayment(paymentId, userId);
        return R.success(result);
    }

    @GetMapping("/status/{paymentId}")
    @ApiOperation("查询支付状态")
    public R<PaymentVO> getPaymentStatus(@PathVariable String paymentId) {
        PaymentVO result = paymentService.getPaymentStatus(paymentId);
        return R.success(result);
    }

    @GetMapping("/detail/{paymentId}")
    @ApiOperation("获取支付详情")
    public R<PaymentVO> getPaymentDetail(@PathVariable Long paymentId) {
        Long userId = StpUtil.getLoginIdAsLong();
        PaymentVO result = paymentService.getPaymentDetail(paymentId, userId);
        return R.success(result);
    }

    @GetMapping("/order/{orderId}")
    @ApiOperation("根据订单ID获取支付信息")
    public R<PaymentVO> getPaymentByOrderId(@PathVariable Long orderId) {
        PaymentVO result = paymentService.getPaymentByOrderId(orderId);
        return R.success(result);
    }

    @GetMapping("/history")
    @ApiOperation("获取支付记录列表")
    public R<IPage<PaymentVO>> getPaymentHistory(
            @ApiParam("支付状态") @RequestParam(required = false) String status,
            @ApiParam("支付方式") @RequestParam(required = false) String paymentMethod,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {

        Long userId = StpUtil.getLoginIdAsLong();

        PaymentQueryDTO queryDTO = new PaymentQueryDTO();
        queryDTO.setUserId(userId);
        queryDTO.setStatus(status);
        queryDTO.setPaymentMethod(paymentMethod);

        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNum(pageNum);
        pageQuery.setPageSize(pageSize);

        IPage<PaymentVO> result = paymentService.getPaymentHistory(queryDTO, pageQuery);
        return R.success(result);
    }

    @GetMapping("/balance")
    @ApiOperation("获取用户余额")
    public R<UserBalanceVO> getUserBalance() {
        Long userId = StpUtil.getLoginIdAsLong();
        UserBalanceVO balance = paymentService.getUserBalance(userId);
        return R.success(balance);
    }

    @PostMapping("/balance/pay")
    @ApiOperation("余额支付")
    public R<PaymentVO> payWithBalance(@Valid @RequestBody Map<String, Object> request) {
        Long userId = StpUtil.getLoginIdAsLong();
        Long orderId = Long.valueOf(request.get("orderId").toString());
        String amount = request.get("amount").toString();
        String password = request.get("password").toString();

        PaymentVO result = paymentService.payWithBalance(orderId, amount, password, userId);
        return R.success(result);
    }

    @GetMapping("/methods")
    @ApiOperation("获取支付方式配置")
    public R<List<Map<String, Object>>> getPaymentMethods() {
        List<Map<String, Object>> methods = paymentService.getPaymentMethods();
        return R.success(methods);
    }

    @PostMapping("/refund")
    @ApiOperation("申请退款")
    public R<Boolean> requestRefund(@Valid @RequestBody RefundRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean result = paymentService.requestRefund(request, userId);
        return R.success(result);
    }

    @PostMapping("/cancel/{paymentId}")
    @ApiOperation("取消支付")
    public R<Void> cancelPayment(@PathVariable Long paymentId) {
        Long userId = StpUtil.getLoginIdAsLong();
        paymentService.cancelPayment(paymentId, userId);
        return R.success();
    }

    /**
     * 模拟支付结果（仅用于毕业设计演示）
     */
    @PostMapping("/simulate/{paymentId}")
    @ApiOperation("模拟支付结果（演示用）")
    public R<PaymentVO> simulatePaymentResult(
            @PathVariable String paymentId,
            @RequestBody PaymentSimulateRequest request) {

        PaymentVO result = paymentService.simulatePaymentResult(paymentId, request.getResult());
        return R.success(result);
    }

    /**
     * 模拟支付回调接口（用于演示）
     */
    @PostMapping("/callback/{paymentNo}")
    @ApiOperation("支付回调（模拟）")
    public R<Boolean> paymentCallback(@PathVariable String paymentNo,
                                     @RequestParam(defaultValue = "true") boolean success) {
        boolean result = paymentService.handlePaymentCallback(paymentNo, success);
        return R.success(result);
    }

    /**
     * 模拟快速支付（用于演示，直接成功）
     */
    @PostMapping("/quick-pay/{orderId}")
    @ApiOperation("快速支付（演示用）")
    public R<PaymentVO> quickPay(@PathVariable Long orderId,
                                @RequestParam(defaultValue = "1") Integer paymentMethod) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 创建支付请求
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setOrderId(orderId);
        request.setPaymentMethod(paymentMethod);

        // 获取订单金额
        try {
            // 这里简化处理，实际应该先查询订单
            request.setAmount(new java.math.BigDecimal("99.99")); // 占位金额，实际会在service中验证

            PaymentVO payment = paymentService.createPayment(request, userId);
            PaymentVO result = paymentService.executePayment(payment.getId(), userId);

            log.info("快速支付成功，userId: {}, orderId: {}, paymentNo: {}",
                    userId, orderId, result.getPaymentNo());

            return R.success(result);
        } catch (Exception e) {
            log.error("快速支付失败，userId: {}, orderId: {}", userId, orderId, e);
            return R.error("支付失败：" + e.getMessage());
        }
    }
}