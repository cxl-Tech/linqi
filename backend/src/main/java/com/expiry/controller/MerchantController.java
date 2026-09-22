package com.expiry.controller;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.expiry.common.BusinessException;
import com.expiry.common.Result;
import com.expiry.entity.AfterSale;
import com.expiry.entity.Merchant;
import com.expiry.entity.Orders;
import com.expiry.entity.Product;
import com.expiry.service.*;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/api/merchant")
public class MerchantController {

    @Resource
    private MerchantService merchantService;
    @Resource
    private ProductService productService;
    @Resource
    private OrderService orderService;
    @Resource
    private AfterSaleService afterSaleService;
    @Resource
    private OrderItemService orderItemService;

    @PostMapping("/register")
    public Result<?> register(@RequestBody Merchant merchant) {
        return Result.success(merchantService.register(merchant));
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> params) {
        return Result.success(merchantService.login(params.get("username"), params.get("password")));
    }

    @GetMapping("/info")
    public Result<?> info(HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Merchant merchant = merchantService.getById(merchantId);
        merchant.setPassword(null);
        return Result.success(merchant);
    }

    @GetMapping("/info/public/{id}")
    public Result<?> publicInfo(@PathVariable Long id) {
        Merchant merchant = merchantService.getById(id);
        if (merchant == null) return Result.error("商家不存在");
        Map<String, Object> info = new HashMap<>();
        info.put("id", merchant.getId());
        info.put("shopName", merchant.getShopName());
        info.put("shopLogo", merchant.getShopLogo());
        return Result.success(info);
    }

    @PutMapping("/update")
    public Result<?> update(@RequestBody Merchant merchant, HttpServletRequest request) {
        merchant.setId((Long) request.getAttribute("userId"));
        merchant.setPassword(null);
        merchant.setAuditStatus(null);
        merchantService.updateById(merchant);
        return Result.success();
    }

    @PutMapping("/password")
    public Result<?> updatePassword(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Merchant merchant = merchantService.getById(merchantId);
        if (!merchant.getPassword().equals(DigestUtil.md5Hex(params.get("oldPassword")))) {
            throw new BusinessException("原密码错误");
        }
        merchant.setPassword(DigestUtil.md5Hex(params.get("newPassword")));
        merchantService.updateById(merchant);
        return Result.success("密码修改成功");
    }

    @GetMapping("/stats")
    public Result<?> stats(HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        long productCount = productService.count(new LambdaQueryWrapper<Product>().eq(Product::getMerchantId, merchantId));
        long orderCount = orderService.count(new LambdaQueryWrapper<Orders>().eq(Orders::getMerchantId, merchantId));
        List<Orders> paidOrders = orderService.list(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getMerchantId, merchantId).ge(Orders::getOrderStatus, 1).ne(Orders::getOrderStatus, 4));
        BigDecimal totalSales = paidOrders.stream().map(Orders::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        long nearExpiryCount = productService.count(new LambdaQueryWrapper<Product>()
                .eq(Product::getMerchantId, merchantId).eq(Product::getStatus, 1)
                .eq(Product::getExpiryStatus, 1));
        long lowStockCount = productService.count(new LambdaQueryWrapper<Product>()
                .eq(Product::getMerchantId, merchantId).eq(Product::getStatus, 1)
                .apply("stock <= stock_warning"));
        long expiredCount = productService.count(new LambdaQueryWrapper<Product>()
                .eq(Product::getMerchantId, merchantId)
                .eq(Product::getExpiryStatus, 2));
        long warningCount = productService.count(new LambdaQueryWrapper<Product>()
                .eq(Product::getMerchantId, merchantId)
                .and(w -> w
                        .and(w2 -> w2.eq(Product::getStatus, 1).and(w3 -> w3.apply("stock <= stock_warning").or().eq(Product::getExpiryStatus, 1)))
                        .or().eq(Product::getExpiryStatus, 2)));
        // 待发货订单数
        long pendingShipCount = orderService.count(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getMerchantId, merchantId).eq(Orders::getOrderStatus, 1));
        // 售后待处理数
        List<Long> myOrderIds = orderService.list(new LambdaQueryWrapper<Orders>().eq(Orders::getMerchantId, merchantId).select(Orders::getId))
                .stream().map(Orders::getId).collect(java.util.stream.Collectors.toList());
        long pendingAfterSale = myOrderIds.isEmpty() ? 0 : afterSaleService.count(new LambdaQueryWrapper<AfterSale>()
                .in(AfterSale::getOrderId, myOrderIds).eq(AfterSale::getStatus, 0));
        // 订单状态分布
        String[] statusNames = {"待付款", "待发货", "待收货", "已完成", "已取消"};
        List<Map<String, Object>> orderStatusDist = new ArrayList<>();
        for (int i = 0; i <= 4; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", statusNames[i]);
            item.put("value", orderService.count(new LambdaQueryWrapper<Orders>()
                    .eq(Orders::getMerchantId, merchantId).eq(Orders::getOrderStatus, i)));
            orderStatusDist.add(item);
        }
        // 商品销量TOP5
        List<Product> topProducts = productService.list(new LambdaQueryWrapper<Product>()
                .eq(Product::getMerchantId, merchantId).orderByDesc(Product::getSales).last("LIMIT 5"));
        List<Map<String, Object>> productRank = new ArrayList<>();
        for (Product p : topProducts) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", p.getName());
            item.put("sales", p.getSales() != null ? p.getSales() : 0);
            productRank.add(item);
        }
        // 近7天销售额趋势
        List<Map<String, Object>> salesTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);
            List<Orders> dayOrders = orderService.list(new LambdaQueryWrapper<Orders>()
                    .eq(Orders::getMerchantId, merchantId)
                    .ge(Orders::getOrderStatus, 1).ne(Orders::getOrderStatus, 4)
                    .between(Orders::getCreateTime, start, end));
            BigDecimal dayAmount = dayOrders.stream().map(Orders::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            item.put("amount", dayAmount);
            salesTrend.add(item);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("productCount", productCount);
        data.put("orderCount", orderCount);
        data.put("totalSales", totalSales);
        data.put("nearExpiryCount", nearExpiryCount);
        data.put("lowStockCount", lowStockCount);
        data.put("expiredCount", expiredCount);
        data.put("warningCount", warningCount);
        data.put("pendingShipCount", pendingShipCount);
        data.put("pendingAfterSale", pendingAfterSale);
        data.put("orderStatusDist", orderStatusDist);
        data.put("productRank", productRank);
        data.put("salesTrend", salesTrend);
        return Result.success(data);
    }

    @GetMapping("/stock/warning")
    public Result<?> stockWarning(HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        List<Product> list = productService.list(new LambdaQueryWrapper<Product>()
                .eq(Product::getMerchantId, merchantId)
                .and(w -> w
                        .and(w2 -> w2.eq(Product::getStatus, 1).and(w3 -> w3.apply("stock <= stock_warning").or().eq(Product::getExpiryStatus, 1)))
                        .or().eq(Product::getExpiryStatus, 2))
                .orderByDesc(Product::getCreateTime));
        return Result.success(list);
    }

    @GetMapping("/aftersale/list")
    public Result<?> afterSaleList(@RequestParam(defaultValue = "1") int current,
                                   @RequestParam(defaultValue = "10") int size,
                                   HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        List<Long> orderIds = orderService.list(new LambdaQueryWrapper<Orders>().eq(Orders::getMerchantId, merchantId).select(Orders::getId))
                .stream().map(Orders::getId).collect(java.util.stream.Collectors.toList());
        if (orderIds.isEmpty()) {
            return Result.success(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, size));
        }
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<AfterSale> page = afterSaleService.page(
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, size),
                new LambdaQueryWrapper<AfterSale>().in(AfterSale::getOrderId, orderIds).orderByDesc(AfterSale::getCreateTime));
        // 为每条记录计算申请次序
        java.util.List<Map<String, Object>> enriched = new java.util.ArrayList<>();
        for (AfterSale as : page.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", as.getId());
            item.put("orderId", as.getOrderId());
            item.put("userId", as.getUserId());
            item.put("type", as.getType());
            item.put("reason", as.getReason());
            item.put("description", as.getDescription());
            item.put("images", as.getImages());
            item.put("status", as.getStatus());
            item.put("merchantReply", as.getMerchantReply());
            item.put("adminReply", as.getAdminReply());
            item.put("createTime", as.getCreateTime());
            item.put("updateTime", as.getUpdateTime());
            long prevCount = afterSaleService.count(new LambdaQueryWrapper<AfterSale>()
                    .eq(AfterSale::getOrderId, as.getOrderId())
                    .lt(AfterSale::getId, as.getId()));
            item.put("retryIndex", prevCount + 1);
            enriched.add(item);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("records", enriched);
        result.put("total", page.getTotal());
        return Result.success(result);
    }

    @PutMapping("/aftersale/{id}")
    public Result<?> handleAfterSale(@PathVariable Long id, @RequestBody Map<String, String> params, HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        AfterSale afterSale = afterSaleService.getById(id);
        if (afterSale == null) {
            return Result.error("售后记录不存在");
        }
        if (afterSale.getStatus() != 0) {
            return Result.error("该售后已被处理，不可重复操作");
        }
        Orders order = orderService.getById(afterSale.getOrderId());
        if (order == null || !order.getMerchantId().equals(merchantId)) {
            return Result.error("无权操作此售后");
        }
        int newStatus = Integer.parseInt(params.get("status"));
        afterSale.setMerchantReply(params.get("merchantReply"));
        if (newStatus == 2) {
            // 商家拒绝：售后标记已拒绝，订单恢复为已完成
            afterSale.setStatus(2);
            afterSaleService.updateById(afterSale);
            order.setOrderStatus(3);
            orderService.updateById(order);
        } else if (newStatus == 1) {
            // 商家同意：售后标记已完成，订单恢复为已完成
            afterSale.setStatus(4);
            afterSaleService.updateById(afterSale);
            order.setOrderStatus(3);
            orderService.updateById(order);
            // 退款/退货退款类型 → 恢复库存、减少销量
            if (afterSale.getType() != null && afterSale.getType() <= 2) {
                List<com.expiry.entity.OrderItem> items = orderItemService.listByOrderId(order.getId());
                for (com.expiry.entity.OrderItem item : items) {
                    Product product = productService.getById(item.getProductId());
                    if (product != null) {
                        product.setStock(product.getStock() + item.getQuantity());
                        int newSales = (product.getSales() != null ? product.getSales() : 0) - item.getQuantity();
                        product.setSales(Math.max(newSales, 0));
                        productService.updateById(product);
                    }
                }
            }
        }
        return Result.success();
    }
}
