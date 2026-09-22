package com.expiry.controller;

import com.expiry.common.Result;
import com.expiry.entity.Orders;
import com.expiry.service.OrderItemService;
import com.expiry.service.OrderService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Resource
    private OrderService orderService;
    @Resource
    private OrderItemService orderItemService;

    @PostMapping("/create")
    public Result<?> create(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Long> cartIds = ((List<?>) params.get("cartIds")).stream()
                .map(o -> Long.valueOf(o.toString())).collect(java.util.stream.Collectors.toList());
        Long addressId = Long.valueOf(params.get("addressId").toString());
        String remark = params.get("remark") != null ? params.get("remark").toString() : null;
        return Result.success(orderService.createOrder(userId, cartIds, addressId, remark));
    }

    @PostMapping("/buy-now")
    public Result<?> buyNow(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long productId = Long.valueOf(params.get("productId").toString());
        Integer quantity = Integer.valueOf(params.get("quantity").toString());
        Long addressId = Long.valueOf(params.get("addressId").toString());
        String remark = params.get("remark") != null ? params.get("remark").toString() : null;
        return Result.success(orderService.buyNow(userId, productId, quantity, addressId, remark));
    }

    @PutMapping("/pay/{id}")
    public Result<?> pay(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        orderService.payOrder(id, userId);
        return Result.success("支付成功");
    }

    @PutMapping("/confirm/{id}")
    public Result<?> confirm(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        orderService.confirmReceive(id, userId);
        return Result.success();
    }

    @PutMapping("/cancel/{id}")
    public Result<?> cancel(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        orderService.cancelOrder(id, userId);
        return Result.success();
    }

    @GetMapping("/user/list")
    public Result<?> userList(@RequestParam(defaultValue = "1") int current,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) Integer orderStatus,
                              HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(orderService.userPageList(userId, orderStatus, current, size));
    }

    @GetMapping("/detail/{id}")
    public Result<?> detail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Orders order = orderService.getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.error("订单不存在");
        }
        java.util.List<?> items = orderItemService.listByOrderId(id);
        Map<String, Object> data = new java.util.HashMap<>();
        data.put("order", order);
        data.put("items", items);
        return Result.success(data);
    }

    @GetMapping("/merchant/list")
    public Result<?> merchantList(@RequestParam(defaultValue = "1") int current,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(required = false) Integer orderStatus,
                                  HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        return Result.success(orderService.merchantPageList(merchantId, orderStatus, current, size));
    }

    @GetMapping("/merchant/detail/{id}")
    public Result<?> merchantDetail(@PathVariable Long id, HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Orders order = orderService.getById(id);
        if (order == null || !order.getMerchantId().equals(merchantId)) {
            return Result.error("订单不存在");
        }
        return Result.success(order);
    }

    @PutMapping("/merchant/ship/{id}")
    public Result<?> ship(@PathVariable Long id,
                          @RequestBody Map<String, String> params,
                          HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        orderService.shipOrder(id, merchantId, params.get("expressCompany"), params.get("expressNo"));
        return Result.success();
    }
}
