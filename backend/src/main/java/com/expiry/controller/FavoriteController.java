package com.expiry.controller;

import com.expiry.common.Result;
import com.expiry.service.FavoriteService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    @Resource
    private FavoriteService favoriteService;

    @PostMapping("/toggle/{productId}")
    public Result<?> toggle(@PathVariable Long productId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        favoriteService.toggle(userId, productId);
        return Result.success();
    }

    @GetMapping("/check/{productId}")
    public Result<?> check(@PathVariable Long productId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(favoriteService.isFavorite(userId, productId));
    }

    @GetMapping("/list")
    public Result<?> list(@RequestParam(defaultValue = "1") int current,
                          @RequestParam(defaultValue = "10") int size,
                          HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(favoriteService.pageByUser(userId, current, size));
    }
}
