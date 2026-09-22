package com.expiry.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.expiry.entity.Category;
import com.expiry.mapper.CategoryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService extends ServiceImpl<CategoryMapper, Category> {

    public List<Category> listAll() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getStatus, 1).orderByAsc(Category::getSort).orderByDesc(Category::getCreateTime);
        return this.list(wrapper);
    }

    public List<Category> listAllAdmin() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSort).orderByDesc(Category::getCreateTime);
        return this.list(wrapper);
    }
}
