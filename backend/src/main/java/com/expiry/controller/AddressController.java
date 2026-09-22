package com.expiry.controller;

import com.expiry.common.Result;
import com.expiry.entity.Address;
import com.expiry.service.AddressService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/address")
public class AddressController {

    @Resource
    private AddressService addressService;

    @GetMapping("/list")
    public Result<?> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(addressService.listByUser(userId));
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody Address address, HttpServletRequest request) {
        address.setUserId((Long) request.getAttribute("userId"));
        addressService.save(address);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<?> update(@RequestBody Address address, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Address existing = addressService.getById(address.getId());
        if (existing == null || !existing.getUserId().equals(userId)) {
            return Result.error("地址不存在");
        }
        address.setUserId(userId);
        addressService.updateById(address);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Address existing = addressService.getById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            return Result.error("地址不存在");
        }
        addressService.removeById(id);
        return Result.success();
    }

    @PutMapping("/default/{id}")
    public Result<?> setDefault(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        addressService.setDefault(userId, id);
        return Result.success();
    }
}
