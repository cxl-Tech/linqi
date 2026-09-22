package com.expiry.controller;

import com.expiry.common.Result;
import com.expiry.entity.Category;
import com.expiry.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    @GetMapping("/list")
    public Result<?> list() {
        return Result.success(categoryService.listAll());
    }

    @GetMapping("/listAll")
    public Result<?> listAll() {
        return Result.success(categoryService.listAllAdmin());
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody Category category) {
        categoryService.save(category);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<?> update(@RequestBody Category category) {
        categoryService.updateById(category);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        categoryService.removeById(id);
        return Result.success();
    }
}
