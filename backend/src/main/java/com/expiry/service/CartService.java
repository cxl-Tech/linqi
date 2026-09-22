package com.expiry.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.expiry.common.BusinessException;
import com.expiry.entity.Cart;
import com.expiry.entity.Product;
import com.expiry.mapper.CartMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CartService extends ServiceImpl<CartMapper, Cart> {

    @Resource
    private ProductService productService;

    public void addToCart(Long userId, Long productId, Integer quantity) {
        Product product = productService.getById(productId);
        if (product == null || product.getStatus() != 1 || product.getAuditStatus() != 1) {
            throw new BusinessException("商品不存在或已下架");
        }
        if (product.getExpiryStatus() != null && product.getExpiryStatus() == 2) {
            throw new BusinessException("该商品已过期，无法加入购物车");
        }
        if (product.getStock() < quantity) {
            throw new BusinessException("库存不足");
        }
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId).eq(Cart::getProductId, productId);
        Cart existing = this.getOne(wrapper);
        if (existing != null) {
            int newQty = existing.getQuantity() + quantity;
            if (newQty > product.getStock()) {
                throw new BusinessException("库存不足，当前购物车已有" + existing.getQuantity() + "件");
            }
            existing.setQuantity(newQty);
            this.updateById(existing);
        } else {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setQuantity(quantity);
            this.save(cart);
        }
    }

    /**
     * 获取用户购物车列表（附带商品详情）
     */
    public List<Map<String, Object>> listByUserWithProduct(Long userId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId).orderByDesc(Cart::getCreateTime);
        List<Cart> cartList = this.list(wrapper);
        if (cartList.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量查询所有商品
        Set<Long> productIds = cartList.stream().map(Cart::getProductId).collect(Collectors.toSet());
        Map<Long, Product> productMap = productService.listByIds(productIds)
                .stream().collect(Collectors.toMap(Product::getId, p -> p));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Cart cart : cartList) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", cart.getId());
            item.put("userId", cart.getUserId());
            item.put("productId", cart.getProductId());
            item.put("quantity", cart.getQuantity());
            item.put("createTime", cart.getCreateTime());
            item.put("updateTime", cart.getUpdateTime());
            item.put("product", productMap.get(cart.getProductId()));
            result.add(item);
        }
        return result;
    }

    public void clearByUser(Long userId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        this.remove(wrapper);
    }
}
