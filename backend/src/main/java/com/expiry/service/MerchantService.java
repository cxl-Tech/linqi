package com.expiry.service;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.expiry.common.BusinessException;
import com.expiry.entity.Merchant;
import com.expiry.mapper.MerchantMapper;
import com.expiry.utils.JwtUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class MerchantService extends ServiceImpl<MerchantMapper, Merchant> {

    @Resource
    private JwtUtils jwtUtils;

    public Map<String, Object> register(Merchant merchant) {
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Merchant::getUsername, merchant.getUsername());
        if (this.count(wrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }
        merchant.setPassword(DigestUtil.md5Hex(merchant.getPassword()));
        merchant.setStatus(1);
        merchant.setAuditStatus(0);
        this.save(merchant);
        merchant.setPassword(null);
        Map<String, Object> result = new HashMap<>();
        result.put("merchant", merchant);
        result.put("message", "注册成功，请等待管理员审核");
        return result;
    }

    public Map<String, Object> login(String username, String password) {
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Merchant::getUsername, username);
        Merchant merchant = this.getOne(wrapper);
        if (merchant == null) {
            throw new BusinessException("用户名不存在");
        }
        if (!merchant.getPassword().equals(DigestUtil.md5Hex(password))) {
            throw new BusinessException("密码错误");
        }
        if (merchant.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        if (merchant.getAuditStatus() == 0) {
            throw new BusinessException("账号审核中，请耐心等待");
        }
        if (merchant.getAuditStatus() == 2) {
            throw new BusinessException("账号审核未通过，请联系管理员");
        }
        String token = jwtUtils.generateToken(merchant.getId(), merchant.getUsername(), "merchant");
        merchant.setPassword(null);
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("merchant", merchant);
        return result;
    }

    public Page<Merchant> pageList(int current, int size, String keyword, Integer auditStatus) {
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Merchant::getShopName, keyword)
                    .or().like(Merchant::getUsername, keyword)
                    .or().like(Merchant::getContact, keyword));
        }
        if (auditStatus != null) {
            wrapper.eq(Merchant::getAuditStatus, auditStatus);
        }
        wrapper.orderByDesc(Merchant::getCreateTime);
        return this.page(new Page<>(current, size), wrapper);
    }

    public void audit(Long id, Integer auditStatus) {
        Merchant merchant = this.getById(id);
        if (merchant == null) {
            throw new BusinessException("商家不存在");
        }
        merchant.setAuditStatus(auditStatus);
        this.updateById(merchant);
    }
}
