package org.shyu.marketserviceproduct.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 分类VO
 */
@Data
public class CategoryVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long parentId;
    private String name;
    private Integer sort;
    private String icon;
    private Integer level;
}

