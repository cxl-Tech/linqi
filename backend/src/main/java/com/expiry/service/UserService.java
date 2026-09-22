package com.expiry.service;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.expiry.common.BusinessException;
import com.expiry.entity.User;
import com.expiry.mapper.UserMapper;
import com.expiry.utils.JwtUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    @Resource
    private JwtUtils jwtUtils;

    public Map<String, Object> register(String username, String password, String nickname, String phone) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        if (this.count(wrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(DigestUtil.md5Hex(password));
        user.setNickname(nickname != null ? nickname : username);
        user.setPhone(phone);
        user.setStatus(1);
        this.save(user);

        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), "user");
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return result;
    }

    public Map<String, Object> login(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = this.getOne(wrapper);
        if (user == null) {
            throw new BusinessException("用户名不存在");
        }
        if (!user.getPassword().equals(DigestUtil.md5Hex(password))) {
            throw new BusinessException("密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), "user");
        user.setPassword(null);
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return result;
    }

    public User getInfo(Long userId) {
        User user = this.getById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    public void updateInfo(User user) {
        User existing = this.getById(user.getId());
        if (existing == null) {
            throw new BusinessException("用户不存在");
        }
        existing.setNickname(user.getNickname());
        existing.setPhone(user.getPhone());
        existing.setEmail(user.getEmail());
        existing.setAvatar(user.getAvatar());
        this.updateById(existing);
    }

    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = this.getById(userId);
        if (!user.getPassword().equals(DigestUtil.md5Hex(oldPassword))) {
            throw new BusinessException("原密码错误");
        }
        user.setPassword(DigestUtil.md5Hex(newPassword));
        this.updateById(user);
    }

    public Page<User> pageList(int current, int size, String keyword, Integer status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getPhone, keyword));
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreateTime);
        return this.page(new Page<>(current, size), wrapper);
    }
}
