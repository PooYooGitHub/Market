package org.shyu.marketserviceproduct.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品列表VO
 */
@Data
public class ProductListVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Long categoryId;
    private String categoryName;
    private Integer status;
    private Integer viewCount;
    private LocalDateTime createTime;

    /**
     * 封面图片
     */
    private String coverImage;
}

