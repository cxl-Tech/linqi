package com.expiry.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.expiry.entity.AfterSale;
import com.expiry.entity.Orders;
import com.expiry.mapper.AfterSaleMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class AfterSaleService extends ServiceImpl<AfterSaleMapper, AfterSale> {

    @Resource
    private OrderService orderService;

    public void apply(AfterSale afterSale) {
        Orders order = orderService.getById(afterSale.getOrderId());
        if (order == null || !order.getUserId().equals(afterSale.getUserId())) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getOrderStatus() == 5) {
            throw new RuntimeException("该订单已在售后处理中");
        }
        if (order.getOrderStatus() != 2 && order.getOrderStatus() != 3) {
            throw new RuntimeException("当前订单状态不支持申请售后");
        }
        // 已有同意/已完成的售后 → 不可再申请
        long approvedCount = this.count(new LambdaQueryWrapper<AfterSale>()
                .eq(AfterSale::getOrderId, afterSale.getOrderId())
                .in(AfterSale::getStatus, 1, 4));
        if (approvedCount > 0) {
            throw new RuntimeException("该订单售后已处理完成，不可重复申请");
        }
        // 有待处理的 → 不可重复
        long pendingCount = this.count(new LambdaQueryWrapper<AfterSale>()
                .eq(AfterSale::getOrderId, afterSale.getOrderId())
                .eq(AfterSale::getStatus, 0));
        if (pendingCount > 0) {
            throw new RuntimeException("该订单已有待处理的售后申请");
        }
        // 被拒绝次数 ≥ 2 → 不可再申请
        long rejectedCount = this.count(new LambdaQueryWrapper<AfterSale>()
                .eq(AfterSale::getOrderId, afterSale.getOrderId())
                .eq(AfterSale::getStatus, 2));
        if (rejectedCount >= 2) {
            throw new RuntimeException("该订单售后申请次数已达上限");
        }
        order.setOrderStatus(5);
        orderService.updateById(order);
        afterSale.setStatus(0);
        this.save(afterSale);
    }

    /**
     * 查询订单的售后是否可以申请（供前端判断按钮显示）
     */
    public java.util.Map<String, Object> canApply(Long orderId) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        long approvedCount = this.count(new LambdaQueryWrapper<AfterSale>()
                .eq(AfterSale::getOrderId, orderId)
                .in(AfterSale::getStatus, 1, 4));
        long pendingCount = this.count(new LambdaQueryWrapper<AfterSale>()
                .eq(AfterSale::getOrderId, orderId)
                .eq(AfterSale::getStatus, 0));
        long rejectedCount = this.count(new LambdaQueryWrapper<AfterSale>()
                .eq(AfterSale::getOrderId, orderId)
                .eq(AfterSale::getStatus, 2));
        boolean canApply = approvedCount == 0 && pendingCount == 0 && rejectedCount < 2;
        result.put("canApply", canApply);
        result.put("rejectedCount", rejectedCount);
        return result;
    }

    public Page<AfterSale> pageByUser(Long userId, int current, int size) {
        return this.page(new Page<>(current, size),
                new LambdaQueryWrapper<AfterSale>().eq(AfterSale::getUserId, userId).orderByDesc(AfterSale::getCreateTime));
    }

    public Page<AfterSale> pageAll(int current, int size, Integer status) {
        LambdaQueryWrapper<AfterSale> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(AfterSale::getStatus, status);
        }
        wrapper.orderByDesc(AfterSale::getCreateTime);
        return this.page(new Page<>(current, size), wrapper);
    }
}
