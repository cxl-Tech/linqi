package com.expiry.controller;

import com.expiry.common.Result;
import com.expiry.entity.Review;
import com.expiry.service.ReviewService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    @Resource
    private ReviewService reviewService;

    @GetMapping("/product/{productId}")
    public Result<?> listByProduct(@PathVariable Long productId,
                                   @RequestParam(defaultValue = "1") int current,
                                   @RequestParam(defaultValue = "10") int size) {
        return Result.success(reviewService.pageByProduct(productId, current, size));
    }

    @GetMapping("/check")
    public Result<?> check(@RequestParam Long orderId, @RequestParam Long productId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(reviewService.hasReviewed(userId, orderId, productId));
    }

    @GetMapping("/order/{orderId}")
    public Result<?> listByOrder(@PathVariable Long orderId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(reviewService.listByOrder(userId, orderId));
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody Review review, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        review.setUserId(userId);
        reviewService.addReview(review, userId);
        return Result.success();
    }

    @PutMapping("/append/{id}")
    public Result<?> append(@PathVariable Long id, @RequestBody Map<String, String> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        reviewService.appendReview(id, params.get("appendContent"), userId);
        return Result.success();
    }

    @PutMapping("/reply/{id}")
    public Result<?> reply(@PathVariable Long id, @RequestBody Map<String, String> params) {
        reviewService.reply(id, params.get("merchantReply"));
        return Result.success();
    }

    @GetMapping("/user/list")
    public Result<?> userList(@RequestParam(defaultValue = "1") int current,
                              @RequestParam(defaultValue = "10") int size,
                              HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(reviewService.pageByUser(userId, current, size));
    }

    @GetMapping("/merchant/list")
    public Result<?> merchantList(@RequestParam(defaultValue = "1") int current,
                                  @RequestParam(defaultValue = "10") int size,
                                  HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        return Result.success(reviewService.pageByMerchantProducts(merchantId, current, size));
    }
}
