package com.expiry.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.expiry.common.BusinessException;
import com.expiry.entity.Product;
import com.expiry.mapper.ProductMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class ProductService extends ServiceImpl<ProductMapper, Product> {

    public Page<Product> pageList(int current, int size, Long categoryId, Long merchantId,
                                  String keyword, Integer expiryStatus, String sortField, String sortOrder) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1).eq(Product::getAuditStatus, 1).ne(Product::getExpiryStatus, 2);
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        if (merchantId != null) {
            wrapper.eq(Product::getMerchantId, merchantId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Product::getName, keyword).or().like(Product::getDescription, keyword));
        }
        if (expiryStatus != null && expiryStatus != 2) {
            wrapper.eq(Product::getExpiryStatus, expiryStatus);
        }
        if ("price".equals(sortField)) {
            wrapper.orderByAsc("asc".equals(sortOrder), Product::getPrice);
            wrapper.orderByDesc("desc".equals(sortOrder), Product::getPrice);
        } else if ("sales".equals(sortField)) {
            wrapper.orderByDesc(Product::getSales);
        } else if (expiryStatus != null && expiryStatus == 1) {
            wrapper.orderByAsc(Product::getExpiryDate);
        } else {
            wrapper.orderByDesc(Product::getCreateTime);
        }
        return this.page(new Page<>(current, size), wrapper);
    }

    public Page<Product> merchantPageList(int current, int size, Long merchantId, String keyword, Integer status) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getMerchantId, merchantId);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Product::getName, keyword);
        }
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }
        wrapper.orderByDesc(Product::getCreateTime);
        return this.page(new Page<>(current, size), wrapper);
    }

    public Page<Product> adminPageList(int current, int size, String keyword, Integer auditStatus, Integer expiryStatus) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Product::getName, keyword);
        }
        if (auditStatus != null) {
            wrapper.eq(Product::getAuditStatus, auditStatus);
        }
        if (expiryStatus != null) {
            wrapper.eq(Product::getExpiryStatus, expiryStatus);
        }
        wrapper.orderByDesc(Product::getCreateTime);
        return this.page(new Page<>(current, size), wrapper);
    }

    public void addProduct(Product product) {
        product.setExpiryDate(product.getProductionDate().plusDays(product.getShelfLifeDays()));
        product.setExpiryStatus(calcExpiryStatus(product));
        product.setSales(0);
        product.setAuditStatus(0);
        this.save(product);
    }

    public void updateProduct(Product product) {
        Product existing = this.getById(product.getId());
        if (existing == null) {
            throw new BusinessException("商品不存在");
        }
        if (product.getProductionDate() != null && product.getShelfLifeDays() != null) {
            product.setExpiryDate(product.getProductionDate().plusDays(product.getShelfLifeDays()));
            product.setExpiryStatus(calcExpiryStatus(product));
        }
        this.updateById(product);
    }

    public int calcExpiryStatus(Product product) {
        LocalDate today = LocalDate.now();
        if (today.isAfter(product.getExpiryDate())) {
            return 2;
        }
        long remainDays = ChronoUnit.DAYS.between(today, product.getExpiryDate());
        long threshold = product.getShelfLifeDays() / 3;
        if (remainDays <= threshold) {
            return 1;
        }
        return 0;
    }

    public boolean deductStock(Long productId, int quantity) {
        return this.baseMapper.deductStock(productId, quantity) > 0;
    }

    public void updateExpiryStatus() {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(Product::getExpiryStatus, 2);
        java.util.List<Product> products = this.list(wrapper);
        for (Product p : products) {
            int newStatus = calcExpiryStatus(p);
            if (newStatus != p.getExpiryStatus()) {
                p.setExpiryStatus(newStatus);
                if (newStatus == 2) {
                    p.setStatus(0);
                }
                this.updateById(p);
            }
        }
    }
}
