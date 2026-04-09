package org.shyu.marketservicetrade.controller;

import cn.dev33.satoken.stp.StpUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.shyu.marketcommon.result.Result;
import org.shyu.marketservicetrade.dto.AddCartRequest;
import org.shyu.marketservicetrade.service.CartService;
import org.shyu.marketservicetrade.vo.CartVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车控制器
 */
@Api(tags = "购物车管理")
@RestController
@RequestMapping("/trade/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @ApiOperation("添加商品到购物车")
    @PostMapping("/add")
    public Result<CartVO> addToCart(@RequestBody @Validated AddCartRequest request) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        CartVO cartVO = cartService.addToCart(request, userId);
        return Result.success(cartVO);
    }

    @ApiOperation("更新购物车商品数量")
    @PutMapping("/quantity/{cartId}")
    public Result<Void> updateQuantity(@PathVariable Long cartId, @RequestParam Integer quantity) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        cartService.updateQuantity(cartId, quantity, userId);
        return Result.success();
    }

    @ApiOperation("删除购物车商品")
    @DeleteMapping("/{cartId}")
    public Result<Void> removeFromCart(@PathVariable Long cartId) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        cartService.removeFromCart(cartId, userId);
        return Result.success();
    }

    @ApiOperation("批量删除购物车商品")
    @DeleteMapping("/batch")
    public Result<Void> removeMultiple(@RequestBody List<Long> cartIds) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        cartService.removeMultiple(cartIds, userId);
        return Result.success();
    }

    @ApiOperation("更新购物车商品选中状态")
    @PutMapping("/selected/{cartId}")
    public Result<Void> updateSelected(@PathVariable Long cartId, @RequestParam Boolean selected) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        cartService.updateSelected(cartId, selected, userId);
        return Result.success();
    }

    @ApiOperation("全选/取消全选")
    @PutMapping("/selectAll")
    public Result<Void> selectAll(@RequestParam Boolean selected) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        cartService.selectAll(selected, userId);
        return Result.success();
    }

    @ApiOperation("获取购物车列表")
    @GetMapping("/list")
    public Result<List<CartVO>> getCartList() {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        List<CartVO> cartList = cartService.getCartList(userId);
        return Result.success(cartList);
    }

    @ApiOperation("清空购物车")
    @DeleteMapping("/clear")
    public Result<Void> clearCart() {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        cartService.clearCart(userId);
        return Result.success();
    }

    @ApiOperation("获取购物车选中商品数量")
    @GetMapping("/selectedCount")
    public Result<Integer> getSelectedCount() {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        Integer count = cartService.getSelectedCount(userId);
        return Result.success(count);
    }
}

