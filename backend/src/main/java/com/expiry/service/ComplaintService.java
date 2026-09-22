package com.expiry.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.expiry.entity.Complaint;
import com.expiry.mapper.ComplaintMapper;
import org.springframework.stereotype.Service;

@Service
public class ComplaintService extends ServiceImpl<ComplaintMapper, Complaint> {

    public Page<Complaint> pageByUser(Long userId, int current, int size) {
        return this.page(new Page<>(current, size),
                new LambdaQueryWrapper<Complaint>().eq(Complaint::getUserId, userId).orderByDesc(Complaint::getCreateTime));
    }

    public Page<Complaint> pageAll(int current, int size, Integer status) {
        LambdaQueryWrapper<Complaint> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Complaint::getStatus, status);
        }
        wrapper.orderByDesc(Complaint::getCreateTime);
        return this.page(new Page<>(current, size), wrapper);
    }
}
