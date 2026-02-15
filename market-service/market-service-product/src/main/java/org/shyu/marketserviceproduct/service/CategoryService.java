package org.shyu.marketserviceproduct.service;

import org.shyu.marketserviceproduct.entity.Category;
import org.shyu.marketserviceproduct.vo.CategoryVO;

import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService {

    /**
     * 获取所有分类
     */
    List<CategoryVO> listAllCategories();

    /**
     * 根据ID获取分类
     */
    Category getCategoryById(Long id);
}

