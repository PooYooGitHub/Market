package org.shyu.marketserviceproduct.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.shyu.marketserviceproduct.entity.Category;
import org.shyu.marketserviceproduct.mapper.CategoryMapper;
import org.shyu.marketserviceproduct.service.CategoryService;
import org.shyu.marketserviceproduct.vo.CategoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}

