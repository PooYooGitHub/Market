package org.shyu.marketserviceproduct.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.shyu.marketcommon.result.Result;
import org.shyu.marketserviceproduct.dto.ProductQueryRequest;
import org.shyu.marketserviceproduct.entity.Product;
import org.shyu.marketserviceproduct.entity.Category;
import org.shyu.marketserviceproduct.service.ProductService;
import org.shyu.marketserviceproduct.service.CategoryService;
import org.shyu.marketserviceproduct.vo.ProductDetailVO;
import org.shyu.marketserviceproduct.vo.ProductListVO;
import org.shyu.marketserviceproduct.vo.ProductStatisticsVO;
import org.shyu.marketserviceproduct.dto.CategoryCreateRequest;
import org.shyu.marketserviceproduct.dto.CategoryUpdateRequest;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

/**
 * 商品管理控制器（管理员专用）
 * 提供商品的管理、审核、统计等功能
 *
 * @author shyu
 * @since 2026-04-05
 */
@Api(tags = "商品管理（管理员）")
@Slf4j
@RestController
@RequestMapping("/product/admin")
@RequiredArgsConstructor
public class ProductAdminController {

    private final ProductService productService;
    private final CategoryService categoryService;

    /**
     * 分页查询所有商品（管理员视图）
     */
    @ApiOperation("分页查询所有商品")
    @GetMapping("/list")
    @SaCheckRole("admin")
    public Result<Page<ProductListVO>> listAllProducts(ProductQueryRequest request) {
        // 管理员可以查看所有商品，包括待审核、已下架等状态
        Page<ProductListVO> page = productService.listAllProductsForAdmin(request);
        return Result.success(page);
    }

    /**
     * 根据ID查询商品详情（管理员视图）
     */
    @ApiOperation("查询商品详情")
    @GetMapping("/{id}")
    @SaCheckRole("admin")
    public Result<ProductDetailVO> getProductById(@PathVariable Long id) {
        ProductDetailVO product = productService.getProductDetailForAdmin(id);
        return Result.success(product);
    }

    /**
     * 审核商品
     */
    @ApiOperation("审核商品")
    @PostMapping("/{id}/audit")
    @SaCheckRole("admin")
    public Result<String> auditProduct(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestParam(required = false) String auditRemark) {

        // status: 1-审核通过, 2-审核拒绝
        productService.auditProduct(id, status, auditRemark);

        String message = status == 1 ? "商品审核通过" : "商品审核拒绝";
        return Result.success(message);
    }

    /**
     * 批量审核商品
     */
    @ApiOperation("批量审核商品")
    @PostMapping("/batch-audit")
    @SaCheckRole("admin")
    public Result<String> batchAuditProducts(@RequestBody List<Long> ids,
                                            @RequestParam Integer status,
                                            @RequestParam(required = false) String auditRemark) {
        productService.batchAuditProducts(ids, status, auditRemark);
        return Result.success("批量审核完成");
    }

    /**
     * 下架商品
     */
    @ApiOperation("下架商品")
    @PutMapping("/{id}/offline")
    @SaCheckRole("admin")
    public Result<String> offlineProduct(@PathVariable Long id,
                                        @RequestParam(required = false) String reason) {
        productService.offlineProduct(id, reason);
        return Result.success("商品已下架");
    }

    /**
     * 批量下架商品
     */
    @ApiOperation("批量下架商品")
    @PutMapping("/batch-offline")
    @SaCheckRole("admin")
    public Result<String> batchOfflineProducts(@RequestBody List<Long> ids,
                                              @RequestParam(required = false) String reason) {
        productService.batchOfflineProducts(ids, reason);
        return Result.success("批量下架完成");
    }

    /**
     * 删除商品（软删除）
     */
    @ApiOperation("删除商品")
    @DeleteMapping("/{id}")
    @SaCheckRole("admin")
    public Result<String> deleteProduct(@PathVariable Long id) {
        productService.deleteProductByAdmin(id);
        return Result.success("商品已删除");
    }

    /**
     * 批量删除商品
     */
    @ApiOperation("批量删除商品")
    @DeleteMapping("/batch")
    @SaCheckRole("admin")
    public Result<String> batchDeleteProducts(@RequestBody List<Long> ids) {
        productService.batchDeleteProducts(ids);
        return Result.success("批量删除完成");
    }

    /**
     * 获取商品统计数据
     */
    @ApiOperation("获取商品统计数据")
    @GetMapping("/statistics")
    @SaCheckRole("admin")
    public Result<ProductStatisticsVO> getProductStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        ProductStatisticsVO statistics = productService.getProductStatistics(startDate, endDate);
        return Result.success(statistics);
    }

    /**
     * 获取商品分类统计
     */
    @ApiOperation("获取商品分类统计")
    @GetMapping("/category-statistics")
    @SaCheckRole("admin")
    public Result<Map<String, Long>> getCategoryStatistics() {
        Map<String, Long> statistics = productService.getCategoryStatistics();
        return Result.success(statistics);
    }

    /**
     * 获取热门商品排行
     */
    @ApiOperation("获取热门商品排行")
    @GetMapping("/hot-products")
    @SaCheckRole("admin")
    public Result<List<ProductListVO>> getHotProducts(@RequestParam(defaultValue = "10") Integer limit) {
        List<ProductListVO> hotProducts = productService.getHotProducts(limit);
        return Result.success(hotProducts);
    }

    // === 分类管理 ===

    /**
     * 获取所有分类
     */
    @ApiOperation("获取所有分类")
    @GetMapping("/categories")
    @SaCheckRole("admin")
    public Result<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }

    /**
     * 创建分类
     */
    @ApiOperation("创建分类")
    @PostMapping("/category")
    @SaCheckRole("admin")
    public Result<String> createCategory(@RequestBody CategoryCreateRequest request) {
        categoryService.createCategory(request);
        return Result.success("分类创建成功");
    }

    /**
     * 更新分类
     */
    @ApiOperation("更新分类")
    @PutMapping("/category/{id}")
    @SaCheckRole("admin")
    public Result<String> updateCategory(@PathVariable Long id,
                                        @RequestBody CategoryUpdateRequest request) {
        categoryService.updateCategory(id, request);
        return Result.success("分类更新成功");
    }

    /**
     * 删除分类
     */
    @ApiOperation("删除分类")
    @DeleteMapping("/category/{id}")
    @SaCheckRole("admin")
    public Result<String> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success("分类删除成功");
    }

    /**
     * 启用/禁用分类
     */
    @ApiOperation("启用/禁用分类")
    @PutMapping("/category/{id}/status")
    @SaCheckRole("admin")
    public Result<String> updateCategoryStatus(@PathVariable Long id,
                                              @RequestParam Integer status) {
        categoryService.updateCategoryStatus(id, status);
        String message = status == 1 ? "分类已启用" : "分类已禁用";
        return Result.success(message);
    }
}