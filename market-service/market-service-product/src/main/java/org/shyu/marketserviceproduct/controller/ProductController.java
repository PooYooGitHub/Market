package org.shyu.marketserviceproduct.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.shyu.marketcommon.result.Result;
import org.shyu.marketserviceproduct.dto.ProductPublishRequest;
import org.shyu.marketserviceproduct.dto.ProductQueryRequest;
import org.shyu.marketserviceproduct.dto.ProductUpdateRequest;
import org.shyu.marketserviceproduct.service.ProductService;
import org.shyu.marketserviceproduct.vo.ProductDetailVO;
import org.shyu.marketserviceproduct.vo.ProductListVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 商品控制器
 */
@Slf4j
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 商品列表
     */
    @GetMapping("/list")
    public Result<Page<ProductListVO>> listProducts(ProductQueryRequest request) {
        Page<ProductListVO> page = productService.listProducts(request);
        return Result.success(page);
    }

    /**
     * 商品详情
     */
    @GetMapping("/detail/{id}")
    public Result<ProductDetailVO> getProductDetail(@PathVariable Long id) {
        ProductDetailVO detail = productService.getProductDetail(id);
        return Result.success(detail);
    }

    /**
     * 发布商品
     */
    @PostMapping("/publish")
    public Result<Long> publishProduct(@RequestBody @Validated ProductPublishRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        Long productId = productService.publishProduct(request, userId);
        return Result.success("发布成功", productId);
    }

    /**
     * 更新商品
     */
    @PutMapping("/update")
    public Result<Void> updateProduct(@RequestBody @Validated ProductUpdateRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        productService.updateProduct(request, userId);
        return Result.success();
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        productService.deleteProduct(id, userId);
        return Result.success();
    }

    /**
     * 我的商品
     */
    @GetMapping("/my")
    public Result<Page<ProductListVO>> getMyProducts(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = StpUtil.getLoginIdAsLong();
        Page<ProductListVO> page = productService.getMyProducts(userId, pageNum, pageSize);
        return Result.success(page);
    }
}

