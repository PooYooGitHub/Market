package org.shyu.marketserviceproduct.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 发布商品请求DTO
 */
@Data
public class ProductPublishRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "商品标题不能为空")
    @Size(max = 100, message = "商品标题不能超过100个字符")
    private String title;

    @NotBlank(message = "商品描述不能为空")
    @Size(max = 2000, message = "商品描述不能超过2000个字符")
    private String description;

    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "商品价格必须大于0")
    @DecimalMax(value = "999999.99", message = "商品价格不能超过999999.99")
    private BigDecimal price;

    @DecimalMin(value = "0.01", message = "原价必须大于0")
    @DecimalMax(value = "999999.99", message = "原价不能超过999999.99")
    private BigDecimal originalPrice;

    @NotNull(message = "商品分类不能为空")
    private Long categoryId;

    /**
     * 商品图片URL列表
     */
    @NotEmpty(message = "商品图片不能为空")
    @Size(min = 1, max = 9, message = "商品图片数量必须在1-9张之间")
    private List<String> imageUrls;
}

