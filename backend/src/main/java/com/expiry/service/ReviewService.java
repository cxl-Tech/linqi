package com.expiry.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.expiry.entity.Product;
import com.expiry.entity.Review;
import com.expiry.mapper.ReviewMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.expiry.common.BusinessException;
import com.expiry.entity.Orders;

@Service
public class ReviewService extends ServiceImpl<ReviewMapper, Review> {

    @Resource
    private ProductService productService;
    @Resource
    private OrderService orderService;

    public Page<Review> pageByProduct(Long productId, int current, int size) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getProductId, productId).orderByDesc(Review::getCreateTime);
        return this.page(new Page<>(current, size), wrapper);
    }

    public Page<Review> pageByMerchantProducts(Long merchantId, int current, int size) {
        List<Long> productIds = productService.list(
                new LambdaQueryWrapper<Product>().eq(Product::getMerchantId, merchantId).select(Product::getId)
        ).stream().map(Product::getId).collect(Collectors.toList());
        if (productIds.isEmpty()) {
            return new Page<>(current, size);
        }
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Review::getProductId, productIds).orderByDesc(Review::getCreateTime);
        return this.page(new Page<>(current, size), wrapper);
    }

    public Page<Review> pageByUser(Long userId, int current, int size) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getUserId, userId).orderByDesc(Review::getCreateTime);
        return this.page(new Page<>(current, size), wrapper);
    }

    public void reply(Long reviewId, String merchantReply) {
        Review review = this.getById(reviewId);
        review.setMerchantReply(merchantReply);
        review.setReplyTime(LocalDateTime.now());
        this.updateById(review);
    }

    public boolean hasReviewed(Long userId, Long orderId, Long productId) {
        return this.count(new LambdaQueryWrapper<Review>()
                .eq(Review::getUserId, userId)
                .eq(Review::getOrderId, orderId)
                .eq(Review::getProductId, productId)) > 0;
    }

    public List<Review> listByOrder(Long userId, Long orderId) {
        return this.list(new LambdaQueryWrapper<Review>()
                .eq(Review::getUserId, userId)
                .eq(Review::getOrderId, orderId)
                .orderByAsc(Review::getCreateTime));
    }

    public void appendReview(Long reviewId, String appendContent, Long userId) {
        Review review = this.getById(reviewId);
        if (review == null || !review.getUserId().equals(userId)) {
            throw new BusinessException("评价不存在");
        }
        if (review.getAppendContent() != null && !review.getAppendContent().isEmpty()) {
            throw new BusinessException("已追评过，不可重复追评");
        }
        review.setAppendContent(appendContent);
        review.setAppendTime(LocalDateTime.now());
        this.updateById(review);
    }

    public void addReview(Review review, Long userId) {
        if (review.getOrderId() != null) {
            Orders order = orderService.getById(review.getOrderId());
            if (order == null || !order.getUserId().equals(userId)) {
                throw new BusinessException("订单不存在");
            }
            if (order.getOrderStatus() != 3) {
                throw new BusinessException("只能对已完成的订单进行评价");
            }
            long exists = this.count(new LambdaQueryWrapper<Review>()
                    .eq(Review::getUserId, userId)
                    .eq(Review::getOrderId, review.getOrderId())
                    .eq(Review::getProductId, review.getProductId()));
            if (exists > 0) {
                throw new BusinessException("该商品已评价，请勿重复提交");
            }
        }
        this.save(review);
    }
}
