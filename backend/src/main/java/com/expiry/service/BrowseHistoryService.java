package com.expiry.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.expiry.entity.BrowseHistory;
import com.expiry.mapper.BrowseHistoryMapper;
import org.springframework.stereotype.Service;

@Service
public class BrowseHistoryService extends ServiceImpl<BrowseHistoryMapper, BrowseHistory> {

    public void record(Long userId, Long productId) {
        LambdaQueryWrapper<BrowseHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BrowseHistory::getUserId, userId).eq(BrowseHistory::getProductId, productId);
        this.remove(wrapper);
        BrowseHistory history = new BrowseHistory();
        history.setUserId(userId);
        history.setProductId(productId);
        this.save(history);
    }

    public Page<BrowseHistory> pageByUser(Long userId, int current, int size) {
        LambdaQueryWrapper<BrowseHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BrowseHistory::getUserId, userId).orderByDesc(BrowseHistory::getCreateTime);
        return this.page(new Page<>(current, size), wrapper);
    }
}
