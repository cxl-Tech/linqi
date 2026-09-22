package com.expiry.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.expiry.common.BusinessException;
import com.expiry.entity.*;
import com.expiry.mapper.OrdersMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService extends ServiceImpl<OrdersMapper, Orders> {

    @Resource
    private CartService cartService;
    @Resource
    private ProductService productService;
    @Resource
    private OrderItemService orderItemService;
    @Resource
    private AddressService addressService;

    @Transactional
    public List<Orders> createOrder(Long userId, List<Long> cartIds, Long addressId, String remark) {
        Address address = addressService.getById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("收货地址不存在");
        }
        List<Cart> carts = cartService.listByIds(cartIds);
        if (carts.isEmpty()) {
            throw new BusinessException("购物车为空");
        }
        for (Cart c : carts) {
            if (!c.getUserId().equals(userId)) {
                throw new BusinessException("购物车数据异常");
            }
        }
        Map<Long, Product> productCache = new java.util.HashMap<>();
        for (Cart c : carts) {
            Product p = productService.getById(c.getProductId());
            if (p == null || p.getStatus() != 1 || p.getAuditStatus() != 1) {
                throw new BusinessException("商品【" + (p != null ? p.getName() : c.getProductId()) + "】不存在或已下架");
            }
            if (p.getExpiryStatus() != null && p.getExpiryStatus() == 2) {
                throw new BusinessException("商品【" + p.getName() + "】已过期，无法购买");
            }
            productCache.put(c.getProductId(), p);
        }
        Map<Long, List<Cart>> merchantCartMap = carts.stream()
                .collect(Collectors.groupingBy(c -> productCache.get(c.getProductId()).getMerchantId()));

        List<Orders> orderList = new ArrayList<>();
        for (Map.Entry<Long, List<Cart>> entry : merchantCartMap.entrySet()) {
            Long merchantId = entry.getKey();
            List<Cart> merchantCarts = entry.getValue();

            Orders order = new Orders();
            order.setOrderNo(IdUtil.getSnowflakeNextIdStr());
            order.setUserId(userId);
            order.setMerchantId(merchantId);
            order.setPayStatus(0);
            order.setOrderStatus(0);
            order.setLogisticsStatus(0);
            order.setReceiverName(address.getReceiverName());
            order.setReceiverPhone(address.getPhone());
            order.setReceiverAddress(address.getProvince() + address.getCity() + address.getDistrict() + address.getDetail());
            order.setRemark(remark);

            BigDecimal totalAmount = BigDecimal.ZERO;
            List<OrderItem> items = new ArrayList<>();
            for (Cart cart : merchantCarts) {
                Product product = productCache.get(cart.getProductId());
                if (product.getStock() < cart.getQuantity()) {
                    throw new BusinessException("商品【" + product.getName() + "】库存不足");
                }
                OrderItem item = new OrderItem();
                item.setProductId(product.getId());
                item.setProductName(product.getName());
                item.setProductImage(product.getImages() != null ? product.getImages().split(",")[0] : null);
                item.setPrice(product.getPrice());
                item.setQuantity(cart.getQuantity());
                item.setSubtotal(product.getPrice().multiply(new BigDecimal(cart.getQuantity())));
                totalAmount = totalAmount.add(item.getSubtotal());
                items.add(item);
            }
            order.setTotalAmount(totalAmount);
            this.save(order);

            for (OrderItem item : items) {
                item.setOrderId(order.getId());
            }
            orderItemService.saveBatch(items);

            for (Cart cart : merchantCarts) {
                boolean ok = productService.deductStock(cart.getProductId(), cart.getQuantity());
                if (!ok) {
                    throw new BusinessException("商品库存不足，下单失败");
                }
            }
            orderList.add(order);
        }
        cartService.removeByIds(cartIds);
        return orderList;
    }

    @Transactional
    public Orders buyNow(Long userId, Long productId, Integer quantity, Long addressId, String remark) {
        Address address = addressService.getById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("收货地址不存在");
        }
        Product product = productService.getById(productId);
        if (product == null || product.getStatus() != 1 || product.getAuditStatus() != 1) {
            throw new BusinessException("商品不存在或已下架");
        }
        if (product.getExpiryStatus() != null && product.getExpiryStatus() == 2) {
            throw new BusinessException("该商品已过期，无法购买");
        }
        if (product.getStock() < quantity) {
            throw new BusinessException("商品库存不足");
        }

        Orders order = new Orders();
        order.setOrderNo(IdUtil.getSnowflakeNextIdStr());
        order.setUserId(userId);
        order.setMerchantId(product.getMerchantId());
        order.setPayStatus(0);
        order.setOrderStatus(0);
        order.setLogisticsStatus(0);
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getPhone());
        order.setReceiverAddress(address.getProvince() + address.getCity() + address.getDistrict() + address.getDetail());
        order.setRemark(remark);

        BigDecimal subtotal = product.getPrice().multiply(new BigDecimal(quantity));
        order.setTotalAmount(subtotal);
        this.save(order);

        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setProductImage(product.getImages() != null ? product.getImages().split(",")[0] : null);
        item.setPrice(product.getPrice());
        item.setQuantity(quantity);
        item.setSubtotal(subtotal);
        orderItemService.save(item);

        boolean ok = productService.deductStock(product.getId(), quantity);
        if (!ok) {
            throw new BusinessException("商品库存不足，下单失败");
        }

        return order;
    }

    public void payOrder(Long orderId, Long userId) {
        Orders order = this.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() != 0) {
            throw new BusinessException("订单状态异常");
        }
        order.setPayStatus(1);
        order.setOrderStatus(1);
        order.setPayTime(LocalDateTime.now());
        this.updateById(order);
    }

    public void shipOrder(Long orderId, Long merchantId, String expressCompany, String expressNo) {
        Orders order = this.getById(orderId);
        if (order == null || !order.getMerchantId().equals(merchantId)) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() != 1) {
            throw new BusinessException("订单状态异常，无法发货");
        }
        order.setOrderStatus(2);
        order.setExpressCompany(expressCompany);
        order.setExpressNo(expressNo);
        order.setLogisticsStatus(1);
        order.setShipTime(LocalDateTime.now());
        this.updateById(order);
    }

    public void confirmReceive(Long orderId, Long userId) {
        Orders order = this.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() != 2) {
            throw new BusinessException("订单状态异常");
        }
        order.setOrderStatus(3);
        order.setLogisticsStatus(3);
        order.setReceiveTime(LocalDateTime.now());
        this.updateById(order);
    }

    public void cancelOrder(Long orderId, Long userId) {
        Orders order = this.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() != 0) {
            throw new BusinessException("仅待付款订单可取消");
        }
        order.setOrderStatus(4);
        this.updateById(order);
        List<OrderItem> items = orderItemService.listByOrderId(orderId);
        for (OrderItem item : items) {
            Product product = productService.getById(item.getProductId());
            product.setStock(product.getStock() + item.getQuantity());
            int newSales = (product.getSales() != null ? product.getSales() : 0) - item.getQuantity();
            product.setSales(Math.max(newSales, 0));
            productService.updateById(product);
        }
    }

    public Page<Orders> userPageList(Long userId, Integer orderStatus, int current, int size) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getUserId, userId);
        if (orderStatus != null) {
            wrapper.eq(Orders::getOrderStatus, orderStatus);
        }
        wrapper.orderByDesc(Orders::getCreateTime);
        return this.page(new Page<>(current, size), wrapper);
    }

    public Page<Orders> merchantPageList(Long merchantId, Integer orderStatus, int current, int size) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getMerchantId, merchantId);
        if (orderStatus != null) {
            wrapper.eq(Orders::getOrderStatus, orderStatus);
        }
        wrapper.orderByDesc(Orders::getCreateTime);
        return this.page(new Page<>(current, size), wrapper);
    }

    public Page<Orders> adminPageList(Integer orderStatus, String keyword, int current, int size) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        if (orderStatus != null) {
            wrapper.eq(Orders::getOrderStatus, orderStatus);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Orders::getOrderNo, keyword)
                    .or().like(Orders::getReceiverName, keyword));
        }
        wrapper.orderByDesc(Orders::getCreateTime);
        return this.page(new Page<>(current, size), wrapper);
    }
}
