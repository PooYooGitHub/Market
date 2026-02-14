package org.shyu.marketapitrade.feign;
import org.shyu.marketapitrade.dto.OrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
@FeignClient(name = "market-service-core", path = "/api/trade")
public interface TradeFeignClient {
    @GetMapping("/order/{id}")
    OrderDTO getOrderById(@PathVariable("id") Long id);
}