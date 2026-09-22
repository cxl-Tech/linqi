package com.expiry.controller;

import com.expiry.common.Result;
import com.expiry.entity.Product;
import com.expiry.service.ProductService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Resource
    private ProductService productService;

    @GetMapping("/list")
    public Result<?> list(@RequestParam(defaultValue = "1") int current,
                          @RequestParam(defaultValue = "12") int size,
                          @RequestParam(required = false) Long categoryId,
                          @RequestParam(required = false) Long merchantId,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) Integer expiryStatus,
                          @RequestParam(required = false) String sortField,
                          @RequestParam(required = false) String sortOrder) {
        return Result.success(productService.pageList(current, size, categoryId, merchantId, keyword, expiryStatus, sortField, sortOrder));
    }

    @GetMapping("/detail/{id}")
    public Result<?> detail(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        if (product.getExpiryStatus() != null && product.getExpiryStatus() == 2) {
            return Result.error("该商品已过期，已下架处理");
        }
        return Result.success(product);
    }

    @GetMapping("/merchant/list")
    public Result<?> merchantList(@RequestParam(defaultValue = "1") int current,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) Integer status,
                                  HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        return Result.success(productService.merchantPageList(current, size, merchantId, keyword, status));
    }

    @PostMapping("/merchant/add")
    public Result<?> add(@RequestBody Product product, HttpServletRequest request) {
        if (product.getProductionDate() != null && product.getShelfLifeDays() != null) {
            java.time.LocalDate expiryDate = product.getProductionDate().plusDays(product.getShelfLifeDays());
            if (!expiryDate.isAfter(java.time.LocalDate.now())) {
                return Result.error("该商品已过期，无法新增过期商品");
            }
        }
        product.setMerchantId((Long) request.getAttribute("userId"));
        productService.addProduct(product);
        return Result.success();
    }

    @PutMapping("/merchant/update")
    public Result<?> update(@RequestBody Product product, HttpServletRequest request) {
        product.setMerchantId((Long) request.getAttribute("userId"));
        productService.updateProduct(product);
        return Result.success();
    }

    @PutMapping("/merchant/status/{id}")
    public Result<?> toggleStatus(@PathVariable Long id, HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Product product = productService.getById(id);
        if (product == null || !product.getMerchantId().equals(merchantId)) {
            return Result.error("商品不存在");
        }
        product.setStatus(product.getStatus() == 1 ? 0 : 1);
        productService.updateById(product);
        return Result.success();
    }

    @PutMapping("/merchant/restock/{id}")
    public Result<?> restock(@PathVariable Long id, @RequestBody java.util.Map<String, Integer> params, HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Product product = productService.getById(id);
        if (product == null || !product.getMerchantId().equals(merchantId)) {
            return Result.error("商品不存在");
        }
        if (product.getExpiryStatus() != null && product.getExpiryStatus() == 2) {
            return Result.error("过期商品不可补货");
        }
        Integer quantity = params.get("quantity");
        if (quantity == null || quantity <= 0) {
            return Result.error("补货数量必须大于0");
        }
        product.setStock(product.getStock() + quantity);
        productService.updateById(product);
        return Result.success();
    }

    @PutMapping("/merchant/promote/{id}")
    public Result<?> promote(@PathVariable Long id, @RequestBody java.util.Map<String, Object> params, HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Product product = productService.getById(id);
        if (product == null || !product.getMerchantId().equals(merchantId)) {
            return Result.error("商品不存在");
        }
        Object priceObj = params.get("promotionPrice");
        if (priceObj == null) {
            return Result.error("促销价格不能为空");
        }
        java.math.BigDecimal promotionPrice = new java.math.BigDecimal(priceObj.toString());
        if (promotionPrice.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            return Result.error("促销价格必须大于0");
        }
        if (product.getOriginalPrice() != null && promotionPrice.compareTo(product.getOriginalPrice()) > 0) {
            return Result.error("促销价格不能高于原价");
        }
        product.setPrice(promotionPrice);
        productService.updateById(product);
        return Result.success();
    }

    @DeleteMapping("/merchant/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Product product = productService.getById(id);
        if (product == null || !product.getMerchantId().equals(merchantId)) {
            return Result.error("商品不存在");
        }
        productService.removeById(id);
        return Result.success();
    }
}
