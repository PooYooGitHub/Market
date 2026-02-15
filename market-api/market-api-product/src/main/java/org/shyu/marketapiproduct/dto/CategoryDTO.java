package org.shyu.marketapiproduct.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 分类传输对象
 * 用于服务间调用传输分类信息
 */
@Data
public class CategoryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 分类ID
     */
    private Long id;

    /**
     * 父分类ID (0表示顶级分类)
     */
    private Long parentId;

    /**
     * 分类名称
     */
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
     * 层级
     */
    private Integer level;
}

