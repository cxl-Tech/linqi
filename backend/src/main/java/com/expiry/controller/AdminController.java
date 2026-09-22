package com.expiry.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.expiry.common.Result;
import com.expiry.entity.*;
import com.expiry.service.*;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Resource
    private AdminService adminService;
    @Resource
    private UserService userService;
    @Resource
    private MerchantService merchantService;
    @Resource
    private ProductService productService;
    @Resource
    private OrderService orderService;
    @Resource
    private AfterSaleService afterSaleService;
    @Resource
    private CategoryService categoryService;
    @Resource
    private ComplaintService complaintService;
    @Resource
    private OrderItemService orderItemService;
    @Resource
    private ReviewService reviewService;

    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> params) {
        return Result.success(adminService.login(params.get("username"), params.get("password")));
    }

    @PutMapping("/password")
    public Result<?> updatePassword(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        adminService.updatePassword(adminId, params.get("oldPassword"), params.get("newPassword"));
        return Result.success("密码修改成功");
    }

    @GetMapping("/user/list")
    public Result<?> userList(@RequestParam(defaultValue = "1") int current,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(required = false) Integer status) {
        return Result.success(userService.pageList(current, size, keyword, status));
    }

    @PutMapping("/user/status/{id}")
    public Result<?> toggleUserStatus(@PathVariable Long id) {
        User user = userService.getById(id);
        user.setStatus(user.getStatus() == 1 ? 0 : 1);
        userService.updateById(user);
        return Result.success();
    }

    @GetMapping("/merchant/list")
    public Result<?> merchantList(@RequestParam(defaultValue = "1") int current,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) Integer auditStatus) {
        return Result.success(merchantService.pageList(current, size, keyword, auditStatus));
    }

    @PutMapping("/merchant/audit/{id}")
    public Result<?> auditMerchant(@PathVariable Long id, @RequestParam Integer auditStatus) {
        merchantService.audit(id, auditStatus);
        return Result.success();
    }

    @PutMapping("/merchant/status/{id}")
    public Result<?> toggleMerchantStatus(@PathVariable Long id) {
        Merchant merchant = merchantService.getById(id);
        merchant.setStatus(merchant.getStatus() == 1 ? 0 : 1);
        merchantService.updateById(merchant);
        return Result.success();
    }

    @GetMapping("/product/list")
    public Result<?> productList(@RequestParam(defaultValue = "1") int current,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) Integer auditStatus,
                                 @RequestParam(required = false) Integer expiryStatus) {
        return Result.success(productService.adminPageList(current, size, keyword, auditStatus, expiryStatus));
    }

    @PutMapping("/product/audit/{id}")
    public Result<?> auditProduct(@PathVariable Long id, @RequestParam Integer auditStatus) {
        Product product = productService.getById(id);
        product.setAuditStatus(auditStatus);
        productService.updateById(product);
        return Result.success();
    }

    @PutMapping("/product/status/{id}")
    public Result<?> toggleProductStatus(@PathVariable Long id) {
        Product product2 = productService.getById(id);
        product2.setStatus(product2.getStatus() == 1 ? 0 : 1);
        productService.updateById(product2);
        return Result.success();
    }

    @DeleteMapping("/product/{id}")
    public Result<?> deleteProduct(@PathVariable Long id) {
        productService.removeById(id);
        return Result.success();
    }

    @GetMapping("/order/list")
    public Result<?> orderList(@RequestParam(defaultValue = "1") int current,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(required = false) Integer orderStatus,
                               @RequestParam(required = false) String keyword) {
        return Result.success(orderService.adminPageList(orderStatus, keyword, current, size));
    }

    @GetMapping("/order/detail/{id}")
    public Result<?> orderDetail(@PathVariable Long id) {
        Orders order = orderService.getById(id);
        if (order == null) return Result.error("订单不存在");
        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        data.put("items", orderItemService.listByOrderId(id));
        // 用户信息
        User user = userService.getById(order.getUserId());
        if (user != null) {
            Map<String, Object> u = new HashMap<>();
            u.put("id", user.getId());
            u.put("username", user.getUsername());
            u.put("nickname", user.getNickname());
            u.put("phone", user.getPhone());
            data.put("user", u);
        }
        // 商家信息
        Merchant merchant = merchantService.getById(order.getMerchantId());
        if (merchant != null) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", merchant.getId());
            m.put("shopName", merchant.getShopName());
            m.put("username", merchant.getUsername());
            data.put("merchant", m);
        }
        // 评价列表
        List<Review> reviews = reviewService.list(new LambdaQueryWrapper<Review>()
                .eq(Review::getOrderId, id).orderByAsc(Review::getCreateTime));
        data.put("reviews", reviews);
        // 售后记录
        List<AfterSale> afterSales = afterSaleService.list(new LambdaQueryWrapper<AfterSale>()
                .eq(AfterSale::getOrderId, id).orderByDesc(AfterSale::getCreateTime));
        data.put("afterSales", afterSales);
        return Result.success(data);
    }

    @GetMapping("/aftersale/list")
    public Result<?> afterSaleList(@RequestParam(defaultValue = "1") int current,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(required = false) Integer status) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<AfterSale> page = afterSaleService.pageAll(current, size, status);
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
    public Result<?> handleAfterSale(@PathVariable Long id, @RequestBody Map<String, String> params) {
        AfterSale afterSale = afterSaleService.getById(id);
        if (afterSale == null) return Result.error("售后记录不存在");
        if (afterSale.getStatus() != 0) return Result.error("该售后已被处理，不可重复操作");
        Orders order = orderService.getById(afterSale.getOrderId());
        int newStatus = Integer.parseInt(params.get("status"));
        afterSale.setAdminReply(params.get("adminReply"));
        if (newStatus == 1) {
            // 管理员同意
            afterSale.setStatus(4);
            afterSaleService.updateById(afterSale);
            if (order != null) {
                order.setOrderStatus(3);
                orderService.updateById(order);
                // 退款/退货退款类型 → 恢复库存、减少销量
                if (afterSale.getType() != null && afterSale.getType() <= 2) {
                    List<OrderItem> items = orderItemService.listByOrderId(order.getId());
                    for (OrderItem item : items) {
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
        } else if (newStatus == 2) {
            // 管理员拒绝
            afterSale.setStatus(2);
            afterSaleService.updateById(afterSale);
            if (order != null) {
                order.setOrderStatus(3);
                orderService.updateById(order);
            }
        }
        return Result.success();
    }

    @GetMapping("/complaint/list")
    public Result<?> complaintList(@RequestParam(defaultValue = "1") int current,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(required = false) Integer status) {
        return Result.success(complaintService.pageAll(current, size, status));
    }

    @PutMapping("/complaint/{id}")
    public Result<?> handleComplaint(@PathVariable Long id, @RequestBody Map<String, String> params) {
        Complaint complaint = complaintService.getById(id);
        if (complaint == null) return Result.error("投诉记录不存在");
        complaint.setAdminReply(params.get("adminReply"));
        complaint.setStatus(1);
        complaintService.updateById(complaint);
        return Result.success();
    }

    @GetMapping("/stats")
    public Result<?> stats() {
        Map<String, Object> data = new HashMap<>();
        data.put("userCount", userService.count());
        data.put("merchantCount", merchantService.count());
        data.put("productCount", productService.count());
        data.put("orderCount", orderService.count());
        List<Orders> paidOrders = orderService.list(new LambdaQueryWrapper<Orders>()
                .ge(Orders::getOrderStatus, 1).ne(Orders::getOrderStatus, 4));
        BigDecimal totalSales = paidOrders.stream().map(Orders::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        data.put("totalSales", totalSales);
        data.put("pendingMerchant", merchantService.count(new LambdaQueryWrapper<Merchant>().eq(Merchant::getAuditStatus, 0)));
        data.put("nearExpiryProduct", productService.count(new LambdaQueryWrapper<Product>().eq(Product::getExpiryStatus, 1)));

        // 订单状态分布
        String[] statusNames = {"待付款", "待发货", "待收货", "已完成", "已取消"};
        List<Map<String, Object>> orderStatusDist = new ArrayList<>();
        for (int i = 0; i <= 4; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", statusNames[i]);
            item.put("value", orderService.count(new LambdaQueryWrapper<Orders>().eq(Orders::getOrderStatus, i)));
            orderStatusDist.add(item);
        }
        data.put("orderStatusDist", orderStatusDist);

        // 分类销量分布
        List<Category> categories = categoryService.list();
        List<Map<String, Object>> categorySales = new ArrayList<>();
        for (Category cat : categories) {
            List<Product> catProducts = productService.list(new LambdaQueryWrapper<Product>().eq(Product::getCategoryId, cat.getId()));
            int totalCatSales = catProducts.stream().mapToInt(p -> p.getSales() != null ? p.getSales() : 0).sum();
            Map<String, Object> item = new HashMap<>();
            item.put("name", cat.getName());
            item.put("value", totalCatSales);
            categorySales.add(item);
        }
        data.put("categorySales", categorySales);

        // 商品销量TOP10
        List<Product> topProducts = productService.list(new LambdaQueryWrapper<Product>()
                .orderByDesc(Product::getSales).last("LIMIT 10"));
        List<Map<String, Object>> productRank = topProducts.stream().map(p -> {
            Map<String, Object> item = new HashMap<>();
            item.put("name", p.getName());
            item.put("sales", p.getSales() != null ? p.getSales() : 0);
            return item;
        }).collect(Collectors.toList());
        data.put("productRank", productRank);

        return Result.success(data);
    }
}
