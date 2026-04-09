package org.shyu.marketserviceproduct.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 分类创建请求DTO
 *
 * @author shyu
 * @since 2026-04-05
 */
@Data
public class CategoryCreateRequest {

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    private String name;

    /**
     * 父分类ID（0表示顶级分类）
     */
    @NotNull(message = "父分类ID不能为空")
    private Long parentId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 图标
     */
    private String icon;

    /**
     * 描述
     */
    private String description;
}