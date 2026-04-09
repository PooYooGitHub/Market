package org.shyu.marketserviceproduct.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.shyu.marketapiuser.dto.UserDTO;
import org.shyu.marketapiuser.feign.UserFeignClient;
import org.shyu.marketcommon.exception.BusinessException;
import org.shyu.marketcommon.result.Result;
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
import org.shyu.marketserviceproduct.vo.PlatformStatisticsVO;
import org.shyu.marketserviceproduct.vo.ProductStatisticsVO;
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

    @Autowired
    private UserFeignClient userFeignClient;

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

        // 通过 Feign 调用 User 服务获取卖家信息
        try {
            Result<UserDTO> userResult = userFeignClient.getUserById(product.getSellerId());
            if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                UserDTO userDTO = userResult.getData();
                ProductDetailVO.SellerInfo sellerInfo = new ProductDetailVO.SellerInfo();
                sellerInfo.setId(userDTO.getId());
                sellerInfo.setUsername(userDTO.getUsername());
                sellerInfo.setNickname(userDTO.getNickname());
                sellerInfo.setAvatar(userDTO.getAvatar());
                vo.setSeller(sellerInfo);
                log.debug("成功获取卖家信息: userId={}, username={}", userDTO.getId(), userDTO.getUsername());
            } else {
                log.warn("获取卖家信息失败: sellerId={}, result={}", product.getSellerId(), userResult);
            }
        } catch (Exception e) {
            log.error("调用User服务获取卖家信息失败: sellerId={}", product.getSellerId(), e);
            // 获取卖家信息失败不影响商品详情展示，继续返回
        }

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

    @Override
    public PlatformStatisticsVO getPlatformStatistics() {
        PlatformStatisticsVO statistics = new PlatformStatisticsVO();

        // 统计在售商品数（状态为1的商品）
        LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.eq(Product::getStatus, 1);
        Long productCount = productMapper.selectCount(productWrapper);
        statistics.setProductCount(productCount);

        // 统计已售出商品数（状态为2的商品）作为成功交易数
        LambdaQueryWrapper<Product> soldWrapper = new LambdaQueryWrapper<>();
        soldWrapper.eq(Product::getStatus, 2);
        Long orderCount = productMapper.selectCount(soldWrapper);
        statistics.setOrderCount(orderCount);

        // 通过用户服务获取用户数
        try {
            // 调用用户服务获取用户统计
            Result<Object> userStatsResult = userFeignClient.getUserStatistics();
            if (userStatsResult.getCode() == 200 && userStatsResult.getData() != null) {
                // 将返回的统计数据转换为Map以便访问
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> userStats = (java.util.Map<String, Object>) userStatsResult.getData();
                // 获取活跃用户数作为统计数据（status=1的用户）
                Object activeCountObj = userStats.get("activeCount");
                if (activeCountObj instanceof Number) {
                    statistics.setUserCount(((Number) activeCountObj).longValue());
                } else {
                    // 如果无法获取活跃用户数，使用总用户数
                    Object totalCountObj = userStats.get("totalCount");
                    if (totalCountObj instanceof Number) {
                        statistics.setUserCount(((Number) totalCountObj).longValue());
                    } else {
                        statistics.setUserCount(0L);
                    }
                }
                log.info("成功获取用户统计数据: {}", userStats);
            } else {
                log.warn("用户统计API返回错误: {}", userStatsResult.getMessage());
                statistics.setUserCount(0L);
            }
        } catch (Exception e) {
            log.error("获取用户统计失败", e);
            statistics.setUserCount(0L);
        }

        return statistics;
    }

    // === 管理员专用方法实现 ===

    @Override
    public Page<ProductListVO> listAllProductsForAdmin(ProductQueryRequest request) {
        Page<Product> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // 管理员可以查看所有状态的商品（包括待审核、已下架等）
        if (request.getStatus() != null) {
            wrapper.eq(Product::getStatus, request.getStatus());
        }

        if (StringUtils.isNotBlank(request.getKeyword())) {
            wrapper.and(w -> w.like(Product::getTitle, request.getKeyword())
                    .or().like(Product::getDescription, request.getKeyword()));
        }

        if (request.getCategoryId() != null) {
            wrapper.eq(Product::getCategoryId, request.getCategoryId());
        }

        if (request.getSellerId() != null) {
            wrapper.eq(Product::getSellerId, request.getSellerId());
        }

        wrapper.orderByDesc(Product::getCreateTime);

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
    public ProductDetailVO getProductDetailForAdmin(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 管理员可以查看任何状态的商品详情
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

        // 获取卖家信息
        try {
            Result<UserDTO> userResult = userFeignClient.getUserById(product.getSellerId());
            if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                UserDTO userDTO = userResult.getData();
                ProductDetailVO.SellerInfo sellerInfo = new ProductDetailVO.SellerInfo();
                sellerInfo.setId(userDTO.getId());
                sellerInfo.setUsername(userDTO.getUsername());
                sellerInfo.setNickname(userDTO.getNickname());
                sellerInfo.setAvatar(userDTO.getAvatar());
                vo.setSeller(sellerInfo);
            }
        } catch (Exception e) {
            log.error("获取卖家信息失败: sellerId={}", product.getSellerId(), e);
        }

        return vo;
    }

    @Override
    @Transactional
    public void auditProduct(Long id, Integer status, String auditRemark) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        product.setStatus(status);
        product.setAuditRemark(auditRemark);
        product.setUpdateTime(java.time.LocalDateTime.now());

        productMapper.updateById(product);

        log.info("商品审核完成: productId={}, status={}, remark={}", id, status, auditRemark);
    }

    @Override
    @Transactional
    public void batchAuditProducts(java.util.List<Long> ids, Integer status, String auditRemark) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        for (Long id : ids) {
            auditProduct(id, status, auditRemark);
        }

        log.info("批量审核完成: ids={}, status={}", ids, status);
    }

    @Override
    @Transactional
    public void offlineProduct(Long id, String reason) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        product.setStatus(3); // 3-已下架
        product.setAuditRemark(reason);
        product.setUpdateTime(java.time.LocalDateTime.now());

        productMapper.updateById(product);

        log.info("商品下架完成: productId={}, reason={}", id, reason);
    }

    @Override
    @Transactional
    public void batchOfflineProducts(java.util.List<Long> ids, String reason) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        for (Long id : ids) {
            offlineProduct(id, reason);
        }

        log.info("批量下架完成: ids={}", ids);
    }

    @Override
    @Transactional
    public void deleteProductByAdmin(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 软删除
        product.setDeleted(1);
        product.setUpdateTime(java.time.LocalDateTime.now());

        productMapper.updateById(product);

        log.info("管理员删除商品: productId={}", id);
    }

    @Override
    @Transactional
    public void batchDeleteProducts(java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        for (Long id : ids) {
            deleteProductByAdmin(id);
        }

        log.info("批量删除完成: ids={}", ids);
    }

    @Override
    public org.shyu.marketserviceproduct.vo.ProductStatisticsVO getProductStatistics(String startDate, String endDate) {
        org.shyu.marketserviceproduct.vo.ProductStatisticsVO statistics =
            new org.shyu.marketserviceproduct.vo.ProductStatisticsVO();

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getDeleted, 0);

        // 如果提供了日期范围，添加时间条件
        if (StringUtils.isNotBlank(startDate)) {
            wrapper.ge(Product::getCreateTime, java.time.LocalDateTime.parse(startDate + "T00:00:00"));
        }
        if (StringUtils.isNotBlank(endDate)) {
            wrapper.le(Product::getCreateTime, java.time.LocalDateTime.parse(endDate + "T23:59:59"));
        }

        List<Product> products = productMapper.selectList(wrapper);

        statistics.setTotalProducts((long) products.size());
        statistics.setOnlineProducts(products.stream().mapToLong(p -> p.getStatus() == 1 ? 1 : 0).sum());
        statistics.setPendingAuditProducts(products.stream().mapToLong(p -> p.getStatus() == 0 ? 1 : 0).sum());
        statistics.setOfflineProducts(products.stream().mapToLong(p -> p.getStatus() == 3 ? 1 : 0).sum());
        statistics.setTotalViews(products.stream().mapToLong(p -> p.getViewCount() != null ? p.getViewCount() : 0).sum());

        return statistics;
    }

    @Override
    public java.util.Map<String, Long> getCategoryStatistics() {
        List<Product> products = productMapper.selectList(
            new LambdaQueryWrapper<Product>()
                .eq(Product::getDeleted, 0)
                .eq(Product::getStatus, 1) // 只统计上线的商品
        );

        return products.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                p -> {
                    Category category = categoryService.getCategoryById(p.getCategoryId());
                    return category != null ? category.getName() : "未分类";
                },
                java.util.stream.Collectors.counting()
            ));
    }

    @Override
    public java.util.List<ProductListVO> getHotProducts(Integer limit) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getDeleted, 0)
                .eq(Product::getStatus, 1)
                .orderByDesc(Product::getViewCount)
                .last("LIMIT " + limit);

        List<Product> products = productMapper.selectList(wrapper);

        return products.stream().map(product -> {
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

