package org.shyu.marketservicetrade.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.shyu.marketcommon.model.PageResult;
import org.shyu.marketservicetrade.dto.CreateOrderRequest;
import org.shyu.marketservicetrade.dto.OrderQueryRequest;
import org.shyu.marketservicetrade.dto.OrderRefundRequest;
import org.shyu.marketservicetrade.entity.Order;
import org.shyu.marketservicetrade.vo.OrderVO;
import org.shyu.marketservicetrade.vo.OrderDetailVO;
import org.shyu.marketservicetrade.vo.OrderListVO;
import org.shyu.marketservicetrade.vo.OrderStatisticsVO;
import org.shyu.marketservicetrade.vo.TransactionAnalysisVO;

import java.util.List;
import java.util.Map;

/**
 * 订单服务接口
 */
public interface OrderService extends IService<Order> {

    /**
     * 创建订单
     *
     * @param request 创建订单请求
     * @param userId 用户ID
     * @return 订单VO
     */
    OrderVO createOrder(CreateOrderRequest request, Long userId);

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @param userId 用户ID
     */
    void cancelOrder(Long orderId, Long userId);

    /**
     * 支付订单
     *
     * @param orderId 订单ID
     * @param userId 用户ID
     */
    void payOrder(Long orderId, Long userId);

    /**
     * 确认发货
     *
     * @param orderId 订单ID
     * @param userId 用户ID (卖家)
     */
    void shipOrder(Long orderId, Long userId);

    /**
     * 确认收货
     *
     * @param orderId 订单ID
     * @param userId 用户ID (买家)
     */
    void receiveOrder(Long orderId, Long userId);

    /**
     * 获取订单详情
     *
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 订单VO
     */
    OrderVO getOrderDetail(Long orderId, Long userId);

    /**
     * 分页查询我的订单(买家)
     *
     * @param request 查询条件
     * @param userId 用户ID
     * @return 分页结果
     */
    PageResult<OrderVO> getMyOrders(OrderQueryRequest request, Long userId);

    /**
     * 分页查询我的销售订单(卖家)
     *
     * @param request 查询条件
     * @param userId 用户ID
     * @return 分页结果
     */
    PageResult<OrderVO> getMySalesOrders(OrderQueryRequest request, Long userId);

    /**
     * 根据订单号获取订单
     *
     * @param orderNo 订单号
     * @return 订单实体
     */
    Order getByOrderNo(String orderNo);

    /**
     * 从购物车创建订单
     *
     * @param cartIds 购物车ID列表
     * @param userId 用户ID
     * @return 创建的订单列表
     */
    List<OrderVO> createOrdersFromCart(List<Long> cartIds, Long userId);

    // ==================== 管理员相关方法 ====================

    /**
     * 分页查询所有订单（管理员视图）
     *
     * @param request 查询条件
     * @return 分页结果
     */
    Page<OrderListVO> listAllOrdersForAdmin(OrderQueryRequest request);

    /**
     * 获取订单详情（管理员视图）
     *
     * @param orderId 订单ID
     * @return 订单详情
     */
    OrderDetailVO getOrderDetailForAdmin(Long orderId);

    /**
     * 根据订单号获取订单（管理员视图）
     *
     * @param orderNo 订单号
     * @return 订单详情
     */
    OrderDetailVO getOrderByNoForAdmin(String orderNo);

    /**
     * 管理员取消订单
     *
     * @param orderId 订单ID
     * @param reason 取消原因
     */
    void cancelOrderByAdmin(Long orderId, String reason);

    /**
     * 批量取消订单
     *
     * @param orderIds 订单ID列表
     * @param reason 取消原因
     */
    void batchCancelOrders(List<Long> orderIds, String reason);

    /**
     * 强制完成订单
     *
     * @param orderId 订单ID
     * @param reason 完成原因
     */
    void forceCompleteOrder(Long orderId, String reason);

    /**
     * 处理退款申请
     *
     * @param orderId 订单ID
     * @param request 退款请求
     */
    void processRefund(Long orderId, OrderRefundRequest request);

    /**
     * 获取订单统计数据
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据
     */
    OrderStatisticsVO getOrderStatistics(String startDate, String endDate);

    /**
     * 获取交易分析数据
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param groupBy 分组方式
     * @return 分析数据
     */
    TransactionAnalysisVO getTransactionAnalysis(String startDate, String endDate, String groupBy);

    /**
     * 获取订单状态统计
     *
     * @return 状态统计
     */
    Map<String, Long> getOrderStatusStatistics();

    /**
     * 获取热门商品销售排行
     *
     * @param limit 限制数量
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 销售排行
     */
    List<Map<String, Object>> getHotSalesProducts(Integer limit, String startDate, String endDate);

    /**
     * 获取用户购买力排行
     *
     * @param limit 限制数量
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 购买力排行
     */
    List<Map<String, Object>> getBuyerRanking(Integer limit, String startDate, String endDate);

    /**
     * 获取卖家销售排行
     *
     * @param limit 限制数量
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 销售排行
     */
    List<Map<String, Object>> getSellerRanking(Integer limit, String startDate, String endDate);

    /**
     * 导出订单数据
     *
     * @param request 查询条件
     * @return 导出路径
     */
    String exportOrders(OrderQueryRequest request);
}

