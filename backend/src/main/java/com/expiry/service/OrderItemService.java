package com.expiry.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.expiry.entity.OrderItem;
import com.expiry.mapper.OrderItemMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderItemService extends ServiceImpl<OrderItemMapper, OrderItem> {

    public List<OrderItem> listByOrderId(Long orderId) {
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        return this.list(wrapper);
    }
}
