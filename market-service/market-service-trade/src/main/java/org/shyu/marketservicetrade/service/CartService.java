package org.shyu.marketservicetrade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.shyu.marketservicetrade.dto.AddCartRequest;
import org.shyu.marketservicetrade.entity.Cart;
import org.shyu.marketservicetrade.vo.CartVO;

import java.util.List;

/**
 * 购物车服务接口
 */
public interface CartService extends IService<Cart> {

    /**
     * 添加商品到购物车
     *
     * @param request 添加购物车请求
     * @param userId 用户ID
     * @return 购物车VO
     */
    CartVO addToCart(AddCartRequest request, Long userId);

    /**
     * 更新购物车商品数量
     *
     * @param cartId 购物车ID
     * @param quantity 数量
     * @param userId 用户ID
     */
    void updateQuantity(Long cartId, Integer quantity, Long userId);

    /**
     * 删除购物车商品
     *
     * @param cartId 购物车ID
     * @param userId 用户ID
     */
    void removeFromCart(Long cartId, Long userId);

    /**
     * 批量删除购物车商品
     *
     * @param cartIds 购物车ID列表
     * @param userId 用户ID
     */
    void removeMultiple(List<Long> cartIds, Long userId);

    /**
     * 更新购物车商品选中状态
     *
     * @param cartId 购物车ID
     * @param selected 是否选中
     * @param userId 用户ID
     */
    void updateSelected(Long cartId, Boolean selected, Long userId);

    /**
     * 全选/取消全选
     *
     * @param selected 是否选中
     * @param userId 用户ID
     */
    void selectAll(Boolean selected, Long userId);

    /**
     * 获取用户购物车列表
     *
     * @param userId 用户ID
     * @return 购物车VO列表
     */
    List<CartVO> getCartList(Long userId);

    /**
     * 清空购物车
     *
     * @param userId 用户ID
     */
    void clearCart(Long userId);

    /**
     * 获取购物车选中商品数量
     *
     * @param userId 用户ID
     * @return 选中商品数量
     */
    Integer getSelectedCount(Long userId);
}

