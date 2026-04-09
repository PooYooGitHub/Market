package org.shyu.marketserviceproduct.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.shyu.marketcommon.exception.BusinessException;
import org.shyu.marketserviceproduct.dto.CategoryCreateRequest;
import org.shyu.marketserviceproduct.dto.CategoryUpdateRequest;
import org.shyu.marketserviceproduct.entity.Category;
import org.shyu.marketserviceproduct.mapper.CategoryMapper;
import org.shyu.marketserviceproduct.service.CategoryService;
import org.shyu.marketserviceproduct.vo.CategoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类服务实现
 */
@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<CategoryVO> listAllCategories() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSort);

        List<Category> categories = categoryMapper.selectList(wrapper);

        return categories.stream().map(category -> {
            CategoryVO vo = new CategoryVO();
            BeanUtil.copyProperties(category, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryMapper.selectById(id);
    }

    // === 管理员专用方法实现 ===

    @Override
    public List<Category> getAllCategories() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSort);
        return categoryMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public void createCategory(CategoryCreateRequest request) {
        Category category = new Category();
        BeanUtil.copyProperties(request, category);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());

        categoryMapper.insert(category);
        log.info("创建分类成功: {}", request.getName());
    }

    @Override
    @Transactional
    public void updateCategory(Long id, CategoryUpdateRequest request) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        BeanUtil.copyProperties(request, category);
        category.setUpdateTime(LocalDateTime.now());

        categoryMapper.updateById(category);
        log.info("更新分类成功: id={}", id);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 检查是否有商品使用此分类
        // 这里可以添加检查逻辑

        categoryMapper.deleteById(id);
        log.info("删除分类成功: id={}", id);
    }

    @Override
    @Transactional
    public void updateCategoryStatus(Long id, Integer status) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        category.setStatus(status);
        category.setUpdateTime(LocalDateTime.now());

        categoryMapper.updateById(category);
        log.info("更新分类状态成功: id={}, status={}", id, status);
    }
}

