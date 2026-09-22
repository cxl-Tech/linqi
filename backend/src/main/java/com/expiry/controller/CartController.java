package com.expiry.controller;

import com.expiry.common.Result;
import com.expiry.entity.Cart;
import com.expiry.service.CartService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Resource
    private CartService cartService;

    @PostMapping("/add")
    public Result<?> add(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long productId = Long.valueOf(params.get("productId").toString());
        Integer quantity = Integer.valueOf(params.getOrDefault("quantity", 1).toString());
        cartService.addToCart(userId, productId, quantity);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<?> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(cartService.listByUserWithProduct(userId));
    }

    @PutMapping("/update/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Map<String, Integer> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Cart cart = cartService.getById(id);
        if (cart == null || !cart.getUserId().equals(userId)) {
            return Result.error("购物车记录不存在");
        }
        cart.setQuantity(params.get("quantity"));
        cartService.updateById(cart);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Cart cart = cartService.getById(id);
        if (cart == null || !cart.getUserId().equals(userId)) {
            return Result.error("购物车记录不存在");
        }
        cartService.removeById(id);
        return Result.success();
    }

    @DeleteMapping("/clear")
    public Result<?> clear(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        cartService.clearByUser(userId);
        return Result.success();
    }
}
