package org.shyu.marketapiproduct.dto;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class ProductDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Long categoryId;
    private Long userId;
    private Integer status;
    private Integer viewCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}