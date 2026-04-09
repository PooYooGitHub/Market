package org.shyu.marketservicetrade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shyu.marketapiproduct.dto.ProductDTO;
import org.shyu.marketapiproduct.feign.ProductFeignClient;
import org.shyu.marketapiuser.dto.UserDTO;
import org.shyu.marketapiuser.feign.UserFeignClient;
import org.shyu.marketcommon.exception.BusinessException;
import org.shyu.marketservicetrade.dto.AddCartRequest;
import org.shyu.marketservicetrade.entity.Cart;
import org.shyu.marketservicetrade.mapper.CartMapper;
import org.shyu.marketservicetrade.service.CartService;
import org.shyu.marketservicetrade.vo.CartVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 购物车服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    private final ProductFeignClient productFeignClient;
    private final UserFeignClient userFeignClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CartVO addToCart(AddCartRequest request, Long userId) {
        // 1. 查询商品信息
        ProductDTO product = productFeignClient.getProductById(request.getProductId()).getData();
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 2. 检查商品状态
        if (product.getStatus() != 1) {
            throw new BusinessException("商品已下架或不可售");
        }

        // 3. 不能添加自己的商品到购物车
        if (Objects.equals(product.getSellerId(), userId)) {
            throw new BusinessException("不能添加自己的商品到购物车");
        }

        // 4. 查询购物车是否已有该商品
        Cart existCart = lambdaQuery()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getProductId, request.getProductId())
                .one();

        if (existCart != null) {
            // 二手市场商品唯一，不支持数量累加，直接抛出提示
            throw new BusinessException("商品已在购物车中，二手商品每人限购一件");
        } else {
            // 校园二手市场每个商品只有一件，强制数量为1
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(request.getProductId());
            cart.setQuantity(1); // 强制为1，忽略用户输入的数量
            cart.setSelected(true);
            save(cart);
            return buildCartVO(cart, product);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQuantity(Long cartId, Integer quantity, Long userId) {
        // 二手市场商品唯一，不支持修改数量
        throw new BusinessException("二手商品数量固定为1，无法修改");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFromCart(Long cartId, Long userId) {
        Cart cart = getById(cartId);
        if (cart == null || !Objects.equals(cart.getUserId(), userId)) {
            throw new BusinessException("购物车商品不存在");
        }

        removeById(cartId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMultiple(List<Long> cartIds, Long userId) {
        if (cartIds == null || cartIds.isEmpty()) {
            return;
        }

        // 验证所有购物车项都属于当前用户
        long count = lambdaQuery()
                .eq(Cart::getUserId, userId)
                .in(Cart::getId, cartIds)
                .count();

        if (count != cartIds.size()) {
            throw new BusinessException("部分购物车商品不存在");
        }

        removeByIds(cartIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSelected(Long cartId, Boolean selected, Long userId) {
        Cart cart = getById(cartId);
        if (cart == null || !Objects.equals(cart.getUserId(), userId)) {
            throw new BusinessException("购物车商品不存在");
        }

        cart.setSelected(selected);
        updateById(cart);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void selectAll(Boolean selected, Long userId) {
        LambdaUpdateWrapper<Cart> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Cart::getUserId, userId)
                .set(Cart::getSelected, selected);
        update(wrapper);
    }

    @Override
    public List<CartVO> getCartList(Long userId) {
        List<Cart> cartList = lambdaQuery()
                .eq(Cart::getUserId, userId)
                .orderByDesc(Cart::getCreateTime)
                .list();

        List<CartVO> voList = new ArrayList<>();
        for (Cart cart : cartList) {
            ProductDTO product = productFeignClient.getProductById(cart.getProductId()).getData();
            if (product != null) {
                CartVO vo = buildCartVO(cart, product);
                voList.add(vo);
            }
        }

        return voList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearCart(Long userId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        remove(wrapper);
    }

    @Override
    public Integer getSelectedCount(Long userId) {
        return lambdaQuery()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getSelected, true)
                .count()
                .intValue();
    }

    /**
     * 构建购物车VO
     */
    private CartVO buildCartVO(Cart cart, ProductDTO product) {
        CartVO vo = new CartVO();
        vo.setId(cart.getId());
        vo.setUserId(cart.getUserId());
        vo.setProductId(cart.getProductId());
        vo.setQuantity(cart.getQuantity());
        vo.setSelected(cart.getSelected());
        vo.setCreateTime(cart.getCreateTime());
        vo.setUpdateTime(cart.getUpdateTime());

        // 商品信息
        if (product != null) {
            vo.setProductTitle(product.getTitle());
            vo.setProductImage(product.getCoverImage());
            vo.setProductPrice(product.getPrice());
            vo.setProductStatus(product.getStatus());
            vo.setSellerId(product.getSellerId());

            // 查询卖家信息
            try {
                UserDTO seller = userFeignClient.getUserById(product.getSellerId()).getData();
                if (seller != null) {
                    vo.setSellerNickname(seller.getNickname());
                }
            } catch (Exception e) {
                log.warn("获取卖家信息失败, sellerId={}", product.getSellerId(), e);
            }
        }

        return vo;
    }
}
