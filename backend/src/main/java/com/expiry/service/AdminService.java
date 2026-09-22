package com.expiry.service;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.expiry.common.BusinessException;
import com.expiry.entity.Admin;
import com.expiry.mapper.AdminMapper;
import com.expiry.utils.JwtUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class AdminService extends ServiceImpl<AdminMapper, Admin> {

    @Resource
    private JwtUtils jwtUtils;

    public Map<String, Object> login(String username, String password) {
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Admin::getUsername, username);
        Admin admin = this.getOne(wrapper);
        if (admin == null) {
            throw new BusinessException("管理员账号不存在");
        }
        if (!admin.getPassword().equals(DigestUtil.md5Hex(password))) {
            throw new BusinessException("密码错误");
        }
        String token = jwtUtils.generateToken(admin.getId(), admin.getUsername(), "admin");
        admin.setPassword(null);
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("admin", admin);
        return result;
    }

    public void updatePassword(Long adminId, String oldPassword, String newPassword) {
        Admin admin = this.getById(adminId);
        if (!admin.getPassword().equals(DigestUtil.md5Hex(oldPassword))) {
            throw new BusinessException("原密码错误");
        }
        admin.setPassword(DigestUtil.md5Hex(newPassword));
        this.updateById(admin);
    }
}
