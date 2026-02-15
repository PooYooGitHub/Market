package org.shyu.marketserviceproduct.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.shyu.marketserviceproduct.dto.ProductPublishRequest;
import org.shyu.marketserviceproduct.dto.ProductQueryRequest;
import org.shyu.marketserviceproduct.dto.ProductUpdateRequest;
import org.shyu.marketserviceproduct.entity.Product;
import org.shyu.marketserviceproduct.vo.ProductDetailVO;
import org.shyu.marketserviceproduct.vo.ProductListVO;

/**
 * 商品服务接口
 */
public interface ProductService {

    /**
     * 发布商品
     */
    Long publishProduct(ProductPublishRequest request, Long userId);

    /**
     * 更新商品
     */
    void updateProduct(ProductUpdateRequest request, Long userId);

    /**
     * 删除商品（软删除）
     */
    void deleteProduct(Long id, Long userId);

    /**
     * 商品列表（分页+搜索）
     */
    Page<ProductListVO> listProducts(ProductQueryRequest request);

    /**
     * 商品详情
     */
    ProductDetailVO getProductDetail(Long id);

    /**
     * 我的商品
     */
    Page<ProductListVO> getMyProducts(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取商品信息（用于Feign调用）
     */
    Product getProductById(Long id);

    /**
     * 增加浏览量
     */
    void increaseViewCount(Long id);
}

