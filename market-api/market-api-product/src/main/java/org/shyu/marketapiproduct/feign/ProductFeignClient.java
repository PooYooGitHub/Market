package org.shyu.marketapiproduct.feign;
import org.shyu.marketapiproduct.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
@FeignClient(name = "market-service-core", path = "/api/product")
public interface ProductFeignClient {
    @GetMapping("/{id}")
    ProductDTO getProductById(@PathVariable("id") Long id);
}