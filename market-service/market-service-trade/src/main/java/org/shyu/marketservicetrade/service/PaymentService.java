package org.shyu.marketservicetrade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.shyu.marketcommon.model.PageQuery;
import org.shyu.marketservicetrade.dto.CreatePaymentRequest;
import org.shyu.marketservicetrade.dto.PaymentQueryDTO;
import org.shyu.marketservicetrade.dto.RefundRequest;
import org.shyu.marketservicetrade.entity.Payment;
import org.shyu.marketservicetrade.vo.PaymentVO;
import org.shyu.marketservicetrade.vo.UserBalanceVO;

import java.util.List;
import java.util.Map;

/**
 * 支付服务接口
 */
public interface PaymentService extends IService<Payment> {

    /**
     * 创建支付
     *
     * @param request 支付请求
     * @param userId 用户ID
     * @return 支付信息
     */
    PaymentVO createPayment(CreatePaymentRequest request, Long userId);

    /**
     * 执行支付（模拟）
     *
     * @param paymentId 支付ID
     * @param userId 用户ID
     * @return 支付结果
     */
    PaymentVO executePayment(Long paymentId, Long userId);

    /**
     * 查询支付状态
     *
     * @param paymentId 支付ID
     * @return 支付信息
     */
    PaymentVO getPaymentStatus(String paymentId);

    /**
     * 获取支付详情
     *
     * @param paymentId 支付ID
     * @param userId 用户ID
     * @return 支付信息
     */
    PaymentVO getPaymentDetail(Long paymentId, Long userId);

    /**
     * 根据订单ID获取支付记录
     *
     * @param orderId 订单ID
     * @return 支付信息
     */
    PaymentVO getPaymentByOrderId(Long orderId);

    /**
     * 获取支付记录列表
     *
     * @param queryDTO 查询参数
     * @param pageQuery 分页参数
     * @return 支付记录分页列表
     */
    IPage<PaymentVO> getPaymentHistory(PaymentQueryDTO queryDTO, PageQuery pageQuery);

    /**
     * 获取用户余额
     *
     * @param userId 用户ID
     * @return 余额信息
     */
    UserBalanceVO getUserBalance(Long userId);

    /**
     * 余额支付
     *
     * @param orderId 订单ID
     * @param amount 支付金额
     * @param password 支付密码
     * @param userId 用户ID
     * @return 支付结果
     */
    PaymentVO payWithBalance(Long orderId, String amount, String password, Long userId);

    /**
     * 获取支付方式配置
     *
     * @return 支付方式列表
     */
    List<Map<String, Object>> getPaymentMethods();

    /**
     * 申请退款
     *
     * @param request 退款申请
     * @param userId 用户ID
     * @return 处理结果
     */
    boolean requestRefund(RefundRequest request, Long userId);

    /**
     * 取消支付
     *
     * @param paymentId 支付ID
     * @param userId 用户ID
     */
    void cancelPayment(Long paymentId, Long userId);

    /**
     * 模拟支付结果
     *
     * @param paymentId 支付ID
     * @param result 支付结果
     * @return 支付信息
     */
    PaymentVO simulatePaymentResult(String paymentId, String result);

    /**
     * 模拟支付回调
     *
     * @param paymentNo 支付流水号
     * @param success 是否成功
     * @return 处理结果
     */
    boolean handlePaymentCallback(String paymentNo, boolean success);
}