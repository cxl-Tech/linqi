package com.expiry.controller;

import com.expiry.common.Result;
import com.expiry.entity.AfterSale;
import com.expiry.service.AfterSaleService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/aftersale")
public class AfterSaleController {

    @Resource
    private AfterSaleService afterSaleService;

    @PostMapping("/apply")
    public Result<?> apply(@RequestBody AfterSale afterSale, HttpServletRequest request) {
        afterSale.setUserId((Long) request.getAttribute("userId"));
        afterSaleService.apply(afterSale);
        return Result.success("申请已提交");
    }

    @GetMapping("/user/list")
    public Result<?> userList(@RequestParam(defaultValue = "1") int current,
                              @RequestParam(defaultValue = "10") int size,
                              HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(afterSaleService.pageByUser(userId, current, size));
    }

    @GetMapping("/can-apply")
    public Result<?> canApply(@RequestParam Long orderId) {
        return Result.success(afterSaleService.canApply(orderId));
    }
}
