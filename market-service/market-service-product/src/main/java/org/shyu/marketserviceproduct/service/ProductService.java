package org.shyu.marketserviceproduct.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.shyu.marketserviceproduct.dto.ProductPublishRequest;
import org.shyu.marketserviceproduct.dto.ProductQueryRequest;
import org.shyu.marketserviceproduct.dto.ProductUpdateRequest;
import org.shyu.marketserviceproduct.entity.Product;
import org.shyu.marketserviceproduct.vo.ProductDetailVO;
import org.shyu.marketserviceproduct.vo.ProductListVO;
import org.shyu.marketserviceproduct.vo.PlatformStatisticsVO;
import org.shyu.marketserviceproduct.vo.ProductStatisticsVO;

import java.util.List;
import java.util.Map;

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

    /**
     * 获取平台统计数据
     */
    PlatformStatisticsVO getPlatformStatistics();

    // === 管理员专用方法 ===

    /**
     * 管理员查看所有商品（包括待审核、已下架等状态）
     */
    Page<ProductListVO> listAllProductsForAdmin(ProductQueryRequest request);

    /**
     * 管理员查看商品详情
     */
    ProductDetailVO getProductDetailForAdmin(Long id);

    /**
     * 审核商品
     */
    void auditProduct(Long id, Integer status, String auditRemark);

    /**
     * 批量审核商品
     */
    void batchAuditProducts(List<Long> ids, Integer status, String auditRemark);

    /**
     * 下架商品
     */
    void offlineProduct(Long id, String reason);

    /**
     * 批量下架商品
     */
    void batchOfflineProducts(List<Long> ids, String reason);

    /**
     * 管理员删除商品
     */
    void deleteProductByAdmin(Long id);

    /**
     * 批量删除商品
     */
    void batchDeleteProducts(List<Long> ids);

    /**
     * 获取商品统计数据
     */
    ProductStatisticsVO getProductStatistics(String startDate, String endDate);

    /**
     * 获取商品分类统计
     */
    Map<String, Long> getCategoryStatistics();

    /**
     * 获取热门商品排行
     */
    List<ProductListVO> getHotProducts(Integer limit);
}

