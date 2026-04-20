package org.shyu.marketservicetrade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shyu.marketapicredit.feign.CreditFeignClient;
import org.shyu.marketapiproduct.dto.ProductDTO;
import org.shyu.marketapiproduct.feign.ProductFeignClient;
import org.shyu.marketapiuser.dto.UserDTO;
import org.shyu.marketapiuser.feign.UserFeignClient;
import org.shyu.marketcommon.exception.BusinessException;
import org.shyu.marketcommon.model.PageResult;
import org.shyu.marketservicetrade.dto.CreateOrderRequest;
import org.shyu.marketservicetrade.dto.OrderQueryRequest;
import org.shyu.marketservicetrade.dto.OrderRefundRequest;
import org.shyu.marketservicetrade.entity.Order;
import org.shyu.marketservicetrade.entity.Cart;
import org.shyu.marketservicetrade.enums.OrderStatus;
import org.shyu.marketservicetrade.mapper.OrderMapper;
import org.shyu.marketservicetrade.service.OrderLogService;
import org.shyu.marketservicetrade.service.OrderService;
import org.shyu.marketservicetrade.service.CartService;
import org.shyu.marketservicetrade.vo.OrderVO;
import org.shyu.marketservicetrade.vo.OrderDetailVO;
import org.shyu.marketservicetrade.vo.OrderListVO;
import org.shyu.marketservicetrade.vo.OrderStatisticsVO;
import org.shyu.marketservicetrade.vo.TransactionAnalysisVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.HashMap;

/**
 * 订单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final OrderLogService orderLogService;
    private final ProductFeignClient productFeignClient;
    private final UserFeignClient userFeignClient;
    private final CreditFeignClient creditFeignClient;
    private final CartService cartService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(CreateOrderRequest request, Long userId) {
        // 1. 查询商品信息
        ProductDTO product = productFeignClient.getProductById(request.getProductId()).getData();
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 2. 检查商品状态（1=已发布可购买，2=已售出，3=锁定中）
        if (product.getStatus() != 1) {
            if (product.getStatus() == 2) {
                throw new BusinessException("商品已售出");
            } else if (product.getStatus() == 3) {
                throw new BusinessException("商品已被他人下单锁定");
            } else {
                throw new BusinessException("商品已下架或不可售");
            }
        }

        // 3. 不能购买自己的商品
        if (Objects.equals(product.getSellerId(), userId)) {
            throw new BusinessException("不能购买自己的商品");
        }

        // 4. 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setBuyerId(userId);
        order.setSellerId(product.getSellerId());
        order.setProductId(request.getProductId());
        order.setTotalAmount(product.getPrice());
        order.setStatus(OrderStatus.WAIT_PAY.getCode());

        save(order);

        // 5. 记录订单日志
        orderLogService.saveLog(order.getId(), OrderStatus.WAIT_PAY.getCode(),
                String.valueOf(userId), "创建订单");

        // 6. 立即将商品状态更新为锁定状态(status=3)，防止重复购买
        //    校园二手市场一物一件，下单即锁定，支付后才真正售出
        try {
            productFeignClient.updateProductStatus(request.getProductId(), 3);
        } catch (Exception e) {
            log.error("更新商品状态失败，将回滚订单。productId={}", request.getProductId(), e);
            throw new BusinessException("商品锁定失败，请重试");
        }

        // 7. 返回订单VO
        return buildOrderVO(order, product, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, Long userId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 只有买家和卖家能取消订单
        if (!Objects.equals(order.getBuyerId(), userId) && !Objects.equals(order.getSellerId(), userId)) {
            throw new BusinessException("无权操作");
        }

        // 只有待支付状态可以取消
        if (!Objects.equals(order.getStatus(), OrderStatus.WAIT_PAY.getCode())) {
            throw new BusinessException("订单状态不允许取消");
        }

        order.setStatus(OrderStatus.CANCELLED.getCode());
        updateById(order);

        orderLogService.saveLog(orderId, OrderStatus.CANCELLED.getCode(),
                String.valueOf(userId), "取消订单");

        // 取消订单后恢复商品状态为已发布(status=1)，商品重新上架可被购买
        try {
            productFeignClient.updateProductStatus(order.getProductId(), 1);
        } catch (Exception e) {
            log.warn("恢复商品状态失败，请手动检查。productId={}", order.getProductId(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long orderId, Long userId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 只有买家能支付
        if (!Objects.equals(order.getBuyerId(), userId)) {
            throw new BusinessException("无权操作");
        }

        // 只有待支付状态可以支付
        if (!Objects.equals(order.getStatus(), OrderStatus.WAIT_PAY.getCode())) {
            throw new BusinessException("订单状态不允许支付");
        }

        order.setStatus(OrderStatus.PAID.getCode());
        order.setPayTime(LocalDateTime.now());
        updateById(order);

        orderLogService.saveLog(orderId, OrderStatus.PAID.getCode(),
                String.valueOf(userId), "支付订单");

        // 支付成功后将商品状态更新为已售出(status=2)
        try {
            productFeignClient.updateProductStatus(order.getProductId(), 2);
        } catch (Exception e) {
            log.warn("更新商品状态失败，orderId={}, productId={}", orderId, order.getProductId(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(Long orderId, Long userId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 只有卖家能发货
        if (!Objects.equals(order.getSellerId(), userId)) {
            throw new BusinessException("无权操作");
        }

        // 只有已支付状态可以发货
        if (!Objects.equals(order.getStatus(), OrderStatus.PAID.getCode())) {
            throw new BusinessException("订单状态不允许发货");
        }

        order.setStatus(OrderStatus.SHIPPED.getCode());
        updateById(order);

        orderLogService.saveLog(orderId, OrderStatus.SHIPPED.getCode(),
                String.valueOf(userId), "确认发货");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receiveOrder(Long orderId, Long userId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 只有买家能确认收货
        if (!Objects.equals(order.getBuyerId(), userId)) {
            throw new BusinessException("无权操作");
        }

        // 只有已发货状态可以确认收货
        if (!Objects.equals(order.getStatus(), OrderStatus.SHIPPED.getCode())) {
            throw new BusinessException("订单状态不允许确认收货");
        }

        order.setStatus(OrderStatus.COMPLETED.getCode());
        updateById(order);

        orderLogService.saveLog(orderId, OrderStatus.COMPLETED.getCode(),
                String.valueOf(userId), "确认收货");

        // 确认收货完成，商品已经在支付时设置为已售出(status=2)，这里不需要重复设置

        // 订单完成后初始化买卖双方的信用分（如果还没有的话）
        notifyTradeCompleted(order.getBuyerId(), order.getSellerId(), orderId);
    }

    /**
     * 初始化用户信用分
     * 订单完成时自动为买卖双方初始化信用分，便于后续评价
     */
    private void notifyTradeCompleted(Long buyerId, Long sellerId, Long orderId) {
        try {
            creditFeignClient.onTradeCompleted(orderId, buyerId, sellerId);
            log.info("trade completed credit event sent, buyerId={}, sellerId={}, orderId={}", buyerId, sellerId, orderId);

        } catch (Exception e) {
            log.warn("trade completed credit event failed, buyerId={}, sellerId={}, orderId={}", buyerId, sellerId, orderId, e);
        }
    }

    @Override
    public OrderVO getOrderDetail(Long orderId, Long userId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 只有买家和卖家能查看订单详情
        if (!Objects.equals(order.getBuyerId(), userId) && !Objects.equals(order.getSellerId(), userId)) {
            throw new BusinessException("无权查看");
        }

        // 查询商品信息
        ProductDTO product = productFeignClient.getProductById(order.getProductId()).getData();

        // 查询买家信息
        UserDTO buyer = userFeignClient.getUserById(order.getBuyerId()).getData();

        // 查询卖家信息
        UserDTO seller = userFeignClient.getUserById(order.getSellerId()).getData();

        return buildOrderVO(order, product, buyer, seller);
    }

    @Override
    public PageResult<OrderVO> getMyOrders(OrderQueryRequest request, Long userId) {
        Page<Order> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getBuyerId, userId);

        if (request.getStatus() != null) {
            wrapper.eq(Order::getStatus, request.getStatus());
        }

        if (request.getOrderNo() != null && !request.getOrderNo().isEmpty()) {
            wrapper.like(Order::getOrderNo, request.getOrderNo());
        }

        wrapper.orderByDesc(Order::getCreateTime);

        IPage<Order> orderPage = page(page, wrapper);

        List<OrderVO> voList = new ArrayList<>();
        for (Order order : orderPage.getRecords()) {
            ProductDTO product = productFeignClient.getProductById(order.getProductId()).getData();
            UserDTO seller = userFeignClient.getUserById(order.getSellerId()).getData();
            voList.add(buildOrderVO(order, product, null, seller));
        }

        return new PageResult<>(orderPage.getTotal(), voList);
    }

    @Override
    public PageResult<OrderVO> getMySalesOrders(OrderQueryRequest request, Long userId) {
        Page<Order> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getSellerId, userId);

        if (request.getStatus() != null) {
            wrapper.eq(Order::getStatus, request.getStatus());
        }

        if (request.getOrderNo() != null && !request.getOrderNo().isEmpty()) {
            wrapper.like(Order::getOrderNo, request.getOrderNo());
        }

        wrapper.orderByDesc(Order::getCreateTime);

        IPage<Order> orderPage = page(page, wrapper);

        List<OrderVO> voList = new ArrayList<>();
        for (Order order : orderPage.getRecords()) {
            ProductDTO product = productFeignClient.getProductById(order.getProductId()).getData();
            UserDTO buyer = userFeignClient.getUserById(order.getBuyerId()).getData();
            voList.add(buildOrderVO(order, product, buyer, null));
        }

        return new PageResult<>(orderPage.getTotal(), voList);
    }

    @Override
    public Order getByOrderNo(String orderNo) {
        return lambdaQuery().eq(Order::getOrderNo, orderNo).one();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<OrderVO> createOrdersFromCart(List<Long> cartIds, Long userId) {
        if (cartIds == null || cartIds.isEmpty()) {
            throw new BusinessException("购物车为空");
        }

        List<OrderVO> orders = new ArrayList<>();
        List<Long> successCartIds = new ArrayList<>();

        for (Long cartId : cartIds) {
            try {
                // 获取购物车项
                Cart cart = cartService.getById(cartId);
                if (cart == null || !Objects.equals(cart.getUserId(), userId)) {
                    log.warn("购物车项不存在或无权访问，cartId: {}, userId: {}", cartId, userId);
                    continue;
                }

                // 创建订单请求
                CreateOrderRequest request = new CreateOrderRequest();
                request.setProductId(cart.getProductId());

                // 创建订单
                OrderVO order = createOrder(request, userId);
                orders.add(order);
                successCartIds.add(cartId);

            } catch (Exception e) {
                log.error("从购物车创建订单失败，cartId: {}", cartId, e);
                // 继续处理其他商品，不中断整个流程
            }
        }

        // 删除已成功创建订单的购物车项
        if (!successCartIds.isEmpty()) {
            cartService.removeMultiple(successCartIds, userId);
        }

        if (orders.isEmpty()) {
            throw new BusinessException("所有商品都创建订单失败，请检查商品状态");
        }

        return orders;
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomNum = String.valueOf((int) (Math.random() * 900000) + 100000);
        return "ORD" + timestamp + randomNum;
    }

    /**
     * 构建订单VO
     */
    private OrderVO buildOrderVO(Order order, ProductDTO product, UserDTO buyer, UserDTO seller) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setBuyerId(order.getBuyerId());
        vo.setSellerId(order.getSellerId());
        vo.setProductId(order.getProductId());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setStatusDesc(OrderStatus.getDescByCode(order.getStatus()));
        vo.setCreateTime(order.getCreateTime());
        vo.setPayTime(order.getPayTime());
        vo.setUpdateTime(order.getUpdateTime());

        // 商品信息
        if (product != null) {
            vo.setProductTitle(product.getTitle());
            vo.setProductImage(product.getCoverImage());
            vo.setProductPrice(product.getPrice());
        }

        // 买家信息
        if (buyer != null) {
            vo.setBuyerNickname(buyer.getNickname());
            vo.setBuyerAvatar(buyer.getAvatar());
        }

        // 卖家信息
        if (seller != null) {
            vo.setSellerNickname(seller.getNickname());
            vo.setSellerAvatar(seller.getAvatar());
        }

        return vo;
    }

    // ==================== 管理员相关方法实现 ====================

    @Override
    public Page<OrderListVO> listAllOrdersForAdmin(OrderQueryRequest request) {
        // TODO: 实现管理员查看所有订单的分页查询
        Page<OrderListVO> page = new Page<>(request.getPageNum(), request.getPageSize());
        // 这里应该添加具体的查询逻辑，将Order转换为OrderListVO
        return page;
    }

    @Override
    public OrderDetailVO getOrderDetailForAdmin(Long orderId) {
        // TODO: 实现管理员查看订单详情
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        // 这里应该添加具体的转换逻辑，将Order转换为OrderDetailVO
        return new OrderDetailVO();
    }

    @Override
    public OrderDetailVO getOrderByNoForAdmin(String orderNo) {
        // TODO: 实现根据订单号查询
        Order order = getByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return getOrderDetailForAdmin(order.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrderByAdmin(Long orderId, String reason) {
        // TODO: 实现管理员取消订单
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        order.setStatus(OrderStatus.CANCELLED.getCode());
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);

        // 记录日志
        orderLogService.saveLog(orderId, OrderStatus.CANCELLED.getCode(), "admin",
            "管理员取消订单：" + (reason != null ? reason : ""));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchCancelOrders(List<Long> orderIds, String reason) {
        // TODO: 实现批量取消订单
        for (Long orderId : orderIds) {
            try {
                cancelOrderByAdmin(orderId, reason);
            } catch (Exception e) {
                log.error("批量取消订单失败，orderId={}", orderId, e);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forceCompleteOrder(Long orderId, String reason) {
        // TODO: 实现强制完成订单
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        order.setStatus(OrderStatus.COMPLETED.getCode());
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);

        // 记录日志
        orderLogService.saveLog(orderId, OrderStatus.COMPLETED.getCode(), "admin",
            "管理员强制完成订单：" + (reason != null ? reason : ""));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processRefund(Long orderId, OrderRefundRequest request) {
        // TODO: 实现处理退款申请
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 这里应该添加具体的退款处理逻辑
        log.info("处理退款申请，orderId={}", orderId);
    }

    @Override
    public OrderStatisticsVO getOrderStatistics(String startDate, String endDate) {
        // TODO: 实现订单统计
        return new OrderStatisticsVO();
    }

    @Override
    public TransactionAnalysisVO getTransactionAnalysis(String startDate, String endDate, String groupBy) {
        // TODO: 实现交易分析
        return new TransactionAnalysisVO();
    }

    @Override
    public Map<String, Long> getOrderStatusStatistics() {
        // TODO: 实现订单状态统计
        return new HashMap<>();
    }

    @Override
    public List<Map<String, Object>> getHotSalesProducts(Integer limit, String startDate, String endDate) {
        // TODO: 实现热门商品排行
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getBuyerRanking(Integer limit, String startDate, String endDate) {
        // TODO: 实现买家排行
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getSellerRanking(Integer limit, String startDate, String endDate) {
        // TODO: 实现卖家排行
        return new ArrayList<>();
    }

    @Override
    public String exportOrders(OrderQueryRequest request) {
        // TODO: 实现订单导出
        log.info("导出订单数据，查询条件：{}", request);
        return "/tmp/orders_export_" + System.currentTimeMillis() + ".xlsx";
    }
}

