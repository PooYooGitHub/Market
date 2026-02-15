package org.shyu.marketserviceproduct.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.shyu.marketapiproduct.dto.CategoryDTO;
import org.shyu.marketapiproduct.dto.ProductDTO;
import org.shyu.marketapiproduct.enums.ProductStatus;
import org.shyu.marketcommon.result.Result;
import org.shyu.marketserviceproduct.entity.Category;
import org.shyu.marketserviceproduct.entity.Product;
import org.shyu.marketserviceproduct.entity.ProductImage;
import org.shyu.marketserviceproduct.mapper.CategoryMapper;
import org.shyu.marketserviceproduct.mapper.ProductImageMapper;
import org.shyu.marketserviceproduct.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Feign 内部调用接口
 * 供其他服务通过 Feign 调用
 */
@Slf4j
@RestController
@RequestMapping("/feign/product")
public class ProductFeignController {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductImageMapper productImageMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 根据ID查询商品（供其他服务调用）
     */
    @GetMapping("/{id}")
    public Result<ProductDTO> getProductById(@PathVariable Long id) {
        log.info("[Feign] 查询商品信息，id: {}", id);

        Product product = productMapper.selectById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }

        ProductDTO dto = convertToDTO(product);
        return Result.success(dto);
    }

    /**
     * 批量查询商品信息
     */
    @PostMapping("/batch")
    public Result<List<ProductDTO>> getProductsByIds(@RequestBody List<Long> ids) {
        log.info("[Feign] 批量查询商品信息，ids: {}", ids);

        if (ids == null || ids.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        List<Product> products = productMapper.selectBatchIds(ids);
        List<ProductDTO> dtoList = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return Result.success(dtoList);
    }

    /**
     * 检查商品是否可用（存在且未下架）
     */
    @GetMapping("/check/{id}")
    public Result<Boolean> checkProductAvailable(@PathVariable Long id) {
        log.info("[Feign] 检查商品是否可用，id: {}", id);

        Product product = productMapper.selectById(id);
        if (product == null) {
            return Result.success(false);
        }

        // 商品状态为1（已发布）才可用
        boolean available = ProductStatus.isAvailable(product.getStatus());
        return Result.success(available);
    }

    /**
     * 更新商品状态（如：已售出）
     */
    @PostMapping("/{id}/status/{status}")
    public Result<Void> updateProductStatus(@PathVariable Long id, @PathVariable Integer status) {
        log.info("[Feign] 更新商品状态，id: {}, status: {}", id, status);

        Product product = productMapper.selectById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }

        product.setStatus(status);
        productMapper.updateById(product);

        return Result.success();
    }

    /**
     * 根据ID查询分类信息
     */
    @GetMapping("/category/{id}")
    public Result<CategoryDTO> getCategoryById(@PathVariable Long id) {
        log.info("[Feign] 查询分类信息，id: {}", id);

        Category category = categoryMapper.selectById(id);
        if (category == null) {
            return Result.error("分类不存在");
        }

        CategoryDTO dto = new CategoryDTO();
        BeanUtil.copyProperties(category, dto);
        return Result.success(dto);
    }

    /**
     * 将 Product 转换为 ProductDTO
     */
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        BeanUtil.copyProperties(product, dto);

        // 获取分类名称
        Category category = categoryMapper.selectById(product.getCategoryId());
        if (category != null) {
            dto.setCategoryName(category.getName());
        }

        // 获取商品图片
        LambdaQueryWrapper<ProductImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductImage::getProductId, product.getId())
                .orderByAsc(ProductImage::getSort);
        List<ProductImage> images = productImageMapper.selectList(wrapper);

        if (!images.isEmpty()) {
            List<String> imageUrls = images.stream()
                    .map(ProductImage::getImageUrl)
                    .collect(Collectors.toList());
            dto.setImageUrls(imageUrls);
            dto.setCoverImage(imageUrls.get(0));
        }

        return dto;
    }
}

