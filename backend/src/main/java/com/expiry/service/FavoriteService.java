package com.expiry.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.expiry.entity.Favorite;
import com.expiry.mapper.FavoriteMapper;
import org.springframework.stereotype.Service;

@Service
public class FavoriteService extends ServiceImpl<FavoriteMapper, Favorite> {

    public boolean isFavorite(Long userId, Long productId) {
        return this.count(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId)) > 0;
    }

    public void toggle(Long userId, Long productId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId).eq(Favorite::getProductId, productId);
        Favorite existing = this.getOne(wrapper);
        if (existing != null) {
            this.removeById(existing.getId());
        } else {
            Favorite fav = new Favorite();
            fav.setUserId(userId);
            fav.setProductId(productId);
            this.save(fav);
        }
    }

    public Page<Favorite> pageByUser(Long userId, int current, int size) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId).orderByDesc(Favorite::getCreateTime);
        return this.page(new Page<>(current, size), wrapper);
    }
}
