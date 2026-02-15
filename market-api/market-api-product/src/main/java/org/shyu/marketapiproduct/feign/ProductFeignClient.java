package org.shyu.marketapiproduct.feign;

import org.shyu.marketapiproduct.dto.CategoryDTO;
import org.shyu.marketapiproduct.dto.ProductDTO;
import org.shyu.marketcommon.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Product 服务 Feign 客户端
 * 供其他服务远程调用 Product 服务
 */
@FeignClient(name = "market-service-product", path = "/feign/product")
public interface ProductFeignClient {

    /**
     * 根据ID查询商品详情
     * @param id 商品ID
     * @return 商品信息
     */
    @GetMapping("/{id}")
    Result<ProductDTO> getProductById(@PathVariable("id") Long id);

    /**
     * 批量查询商品信息
     * @param ids 商品ID列表
     * @return 商品信息列表
     */
    @PostMapping("/batch")
    Result<List<ProductDTO>> getProductsByIds(@RequestBody List<Long> ids);

    /**
     * 检查商品是否可用（存在且未下架）
     * @param id 商品ID
     * @return true-可用，false-不可用
     */
    @GetMapping("/check/{id}")
    Result<Boolean> checkProductAvailable(@PathVariable("id") Long id);

    /**
     * 更新商品状态（如：已售出）
     * @param id 商品ID
     * @param status 状态值
     * @return 是否成功
     */
    @PostMapping("/{id}/status/{status}")
    Result<Void> updateProductStatus(@PathVariable("id") Long id, @PathVariable("status") Integer status);

    /**
     * 根据ID查询分类信息
     * @param id 分类ID
     * @return 分类信息
     */
    @GetMapping("/category/{id}")
    Result<CategoryDTO> getCategoryById(@PathVariable("id") Long id);
}

