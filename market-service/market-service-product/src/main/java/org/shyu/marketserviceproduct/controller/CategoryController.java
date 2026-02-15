package org.shyu.marketserviceproduct.controller;

import lombok.extern.slf4j.Slf4j;
import org.shyu.marketcommon.result.Result;
import org.shyu.marketserviceproduct.service.CategoryService;
import org.shyu.marketserviceproduct.vo.CategoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分类控制器
 */
@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取所有分类
     */
    @GetMapping("/list")
    public Result<List<CategoryVO>> listCategories() {
        List<CategoryVO> categories = categoryService.listAllCategories();
        return Result.success(categories);
    }
}

