package org.shyu.marketserviceproduct.service;

import org.shyu.marketserviceproduct.dto.CategoryCreateRequest;
import org.shyu.marketserviceproduct.dto.CategoryUpdateRequest;
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

    // === 管理员专用方法 ===

    /**
     * 获取所有分类（管理员视图）
     */
    List<Category> getAllCategories();

    /**
     * 创建分类
     */
    void createCategory(CategoryCreateRequest request);

    /**
     * 更新分类
     */
    void updateCategory(Long id, CategoryUpdateRequest request);

    /**
     * 删除分类
     */
    void deleteCategory(Long id);

    /**
     * 更新分类状态
     */
    void updateCategoryStatus(Long id, Integer status);
}

