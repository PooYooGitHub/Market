package org.shyu.marketapitrade.feign;

import org.shyu.marketapitrade.dto.OrderDTO;
import org.shyu.marketcommon.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 交易服务 Feign 客户端
 */
@FeignClient(name = "market-service-trade", path = "/feign/trade")
public interface TradeFeignClient {

    /**
     * 根据ID查询订单
     */
    @GetMapping("/order/{id}")
    Result<OrderDTO> getOrderById(@PathVariable("id") Long id);

    /**
     * 根据订单号查询订单
     */
    @GetMapping("/order/no/{orderNo}")
    Result<OrderDTO> getOrderByNo(@PathVariable("orderNo") String orderNo);
}