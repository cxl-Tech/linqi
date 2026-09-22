package com.expiry.controller;

import com.expiry.common.Result;
import com.expiry.entity.User;
import com.expiry.service.UserService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public Result<?> register(@RequestBody Map<String, String> params) {
        return Result.success(userService.register(
                params.get("username"), params.get("password"),
                params.get("nickname"), params.get("phone")));
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> params) {
        return Result.success(userService.login(params.get("username"), params.get("password")));
    }

    @GetMapping("/info")
    public Result<?> info(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(userService.getInfo(userId));
    }

    @PutMapping("/update")
    public Result<?> update(@RequestBody User user, HttpServletRequest request) {
        user.setId((Long) request.getAttribute("userId"));
        userService.updateInfo(user);
        return Result.success();
    }

    @PutMapping("/password")
    public Result<?> updatePassword(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userService.updatePassword(userId, params.get("oldPassword"), params.get("newPassword"));
        return Result.success("密码修改成功");
    }
}
