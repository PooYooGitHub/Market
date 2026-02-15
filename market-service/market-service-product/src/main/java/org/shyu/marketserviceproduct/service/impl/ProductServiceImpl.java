package org.shyu.marketserviceproduct.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.shyu.marketcommon.exception.BusinessException;
import org.shyu.marketserviceproduct.dto.ProductPublishRequest;
import org.shyu.marketserviceproduct.dto.ProductQueryRequest;
import org.shyu.marketserviceproduct.dto.ProductUpdateRequest;
import org.shyu.marketserviceproduct.entity.Category;
import org.shyu.marketserviceproduct.entity.Product;
import org.shyu.marketserviceproduct.entity.ProductImage;
import org.shyu.marketserviceproduct.mapper.ProductImageMapper;
import org.shyu.marketserviceproduct.mapper.ProductMapper;
import org.shyu.marketserviceproduct.service.CategoryService;
import org.shyu.marketserviceproduct.service.ProductService;
import org.shyu.marketserviceproduct.vo.ProductDetailVO;
import org.shyu.marketserviceproduct.vo.ProductListVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品服务实现
 */
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductImageMapper productImageMapper;

    @Autowired
    private CategoryService categoryService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishProduct(ProductPublishRequest request, Long userId) {
        // 验证分类是否存在
        Category category = categoryService.getCategoryById(request.getCategoryId());
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 创建商品
        Product product = new Product();
        product.setSellerId(userId);
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setCategoryId(request.getCategoryId());
        product.setStatus(1); // 1:发布
        product.setViewCount(0);

        productMapper.insert(product);

        // 保存商品图片
        saveProductImages(product.getId(), request.getImageUrls());

        log.info("用户 {} 发布商品成功，商品ID: {}", userId, product.getId());
        return product.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(ProductUpdateRequest request, Long userId) {
        // 查询商品
        Product product = productMapper.selectById(request.getId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 验证权限
        if (!product.getSellerId().equals(userId)) {
            throw new BusinessException("无权修改此商品");
        }

        // 验证分类
        Category category = categoryService.getCategoryById(request.getCategoryId());
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 更新商品信息
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setCategoryId(request.getCategoryId());

        productMapper.updateById(product);

        // 删除旧图片，保存新图片
        LambdaQueryWrapper<ProductImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductImage::getProductId, product.getId());
        productImageMapper.delete(wrapper);
        saveProductImages(product.getId(), request.getImageUrls());

        log.info("用户 {} 更新商品成功，商品ID: {}", userId, product.getId());
    }

    @Override
    public void deleteProduct(Long id, Long userId) {
        // 查询商品
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 验证权限
        if (!product.getSellerId().equals(userId)) {
            throw new BusinessException("无权删除此商品");
        }

        // 软删除（设置状态为3:下架）
        product.setStatus(3);
        productMapper.updateById(product);

        log.info("用户 {} 删除商品成功，商品ID: {}", userId, id);
    }

    @Override
    public Page<ProductListVO> listProducts(ProductQueryRequest request) {
        Page<Product> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // 只查询已发布的商品
        wrapper.eq(Product::getStatus, 1);

        // 关键词搜索（标题或描述）
        if (StringUtils.isNotBlank(request.getKeyword())) {
            wrapper.and(w -> w.like(Product::getTitle, request.getKeyword())
                    .or()
                    .like(Product::getDescription, request.getKeyword()));
        }

        // 分类筛选
        if (request.getCategoryId() != null) {
            wrapper.eq(Product::getCategoryId, request.getCategoryId());
        }

        // 价格范围筛选
        if (request.getMinPrice() != null) {
            wrapper.ge(Product::getPrice, request.getMinPrice());
        }
        if (request.getMaxPrice() != null) {
            wrapper.le(Product::getPrice, request.getMaxPrice());
        }

        // 排序
        if ("price".equals(request.getSortField())) {
            if ("asc".equals(request.getSortOrder())) {
                wrapper.orderByAsc(Product::getPrice);
            } else {
                wrapper.orderByDesc(Product::getPrice);
            }
        } else if ("view_count".equals(request.getSortField())) {
            if ("asc".equals(request.getSortOrder())) {
                wrapper.orderByAsc(Product::getViewCount);
            } else {
                wrapper.orderByDesc(Product::getViewCount);
            }
        } else {
            // 默认按创建时间排序
            if ("asc".equals(request.getSortOrder())) {
                wrapper.orderByAsc(Product::getCreateTime);
            } else {
                wrapper.orderByDesc(Product::getCreateTime);
            }
        }

        Page<Product> productPage = productMapper.selectPage(page, wrapper);

        // 转换为VO
        Page<ProductListVO> voPage = new Page<>(productPage.getCurrent(), productPage.getSize(), productPage.getTotal());
        List<ProductListVO> voList = productPage.getRecords().stream().map(product -> {
            ProductListVO vo = new ProductListVO();
            BeanUtil.copyProperties(product, vo);

            // 获取分类名称
            Category category = categoryService.getCategoryById(product.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }

            // 获取封面图片（第一张）
            LambdaQueryWrapper<ProductImage> imageWrapper = new LambdaQueryWrapper<>();
            imageWrapper.eq(ProductImage::getProductId, product.getId())
                    .orderByAsc(ProductImage::getSort)
                    .last("LIMIT 1");
            ProductImage coverImage = productImageMapper.selectOne(imageWrapper);
            if (coverImage != null) {
                vo.setCoverImage(coverImage.getImageUrl());
            }

            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public ProductDetailVO getProductDetail(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null || product.getStatus() != 1) {
            throw new BusinessException("商品不存在或已下架");
        }

        // 增加浏览量（异步处理更好，这里简化）
        increaseViewCount(id);

        // 转换为VO
        ProductDetailVO vo = new ProductDetailVO();
        BeanUtil.copyProperties(product, vo);

        // 获取分类名称
        Category category = categoryService.getCategoryById(product.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }

        // 获取商品图片
        LambdaQueryWrapper<ProductImage> imageWrapper = new LambdaQueryWrapper<>();
        imageWrapper.eq(ProductImage::getProductId, id)
                .orderByAsc(ProductImage::getSort);
        List<ProductImage> images = productImageMapper.selectList(imageWrapper);
        List<String> imageUrls = images.stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList());
        vo.setImageUrls(imageUrls);

        // 这里应该调用 User 服务获取卖家信息，暂时简化
        // TODO: 通过 Feign 调用 User 服务

        return vo;
    }

    @Override
    public Page<ProductListVO> getMyProducts(Long userId, Integer pageNum, Integer pageSize) {
        Page<Product> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getSellerId, userId)
                .orderByDesc(Product::getCreateTime);

        Page<Product> productPage = productMapper.selectPage(page, wrapper);

        // 转换为VO
        Page<ProductListVO> voPage = new Page<>(productPage.getCurrent(), productPage.getSize(), productPage.getTotal());
        List<ProductListVO> voList = productPage.getRecords().stream().map(product -> {
            ProductListVO vo = new ProductListVO();
            BeanUtil.copyProperties(product, vo);

            // 获取分类名称
            Category category = categoryService.getCategoryById(product.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }

            // 获取封面图片
            LambdaQueryWrapper<ProductImage> imageWrapper = new LambdaQueryWrapper<>();
            imageWrapper.eq(ProductImage::getProductId, product.getId())
                    .orderByAsc(ProductImage::getSort)
                    .last("LIMIT 1");
            ProductImage coverImage = productImageMapper.selectOne(imageWrapper);
            if (coverImage != null) {
                vo.setCoverImage(coverImage.getImageUrl());
            }

            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public Product getProductById(Long id) {
        return productMapper.selectById(id);
    }

    @Override
    public void increaseViewCount(Long id) {
        Product product = productMapper.selectById(id);
        if (product != null) {
            product.setViewCount(product.getViewCount() + 1);
            productMapper.updateById(product);
        }
    }

    /**
     * 保存商品图片
     */
    private void saveProductImages(Long productId, List<String> imageUrls) {
        for (int i = 0; i < imageUrls.size(); i++) {
            ProductImage image = new ProductImage();
            image.setProductId(productId);
            image.setImageUrl(imageUrls.get(i));
            image.setSort(i);
            productImageMapper.insert(image);
        }
    }
}

