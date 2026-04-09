package org.shyu.marketserviceproduct.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 分类更新请求DTO
 *
 * @author shyu
 * @since 2026-04-05
 */
@Data
public class CategoryUpdateRequest {

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    private String name;

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

    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status;
}