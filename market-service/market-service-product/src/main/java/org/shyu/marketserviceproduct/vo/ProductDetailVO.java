package org.shyu.marketserviceproduct.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品详情VO
 */
@Data
public class ProductDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long sellerId;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Long categoryId;
    private String categoryName;
    private Integer status;
    private Integer viewCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 商品图片列表
     */
    private List<String> imageUrls;

    /**
     * 卖家信息
     */
    private SellerInfo seller;

    @Data
    public static class SellerInfo implements Serializable {
        private Long id;
        private String username;
        private String nickname;
        private String avatar;
    }
}

