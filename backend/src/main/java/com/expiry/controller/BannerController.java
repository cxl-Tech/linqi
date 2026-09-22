package com.expiry.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.expiry.common.Result;
import com.expiry.entity.Banner;
import com.expiry.service.BannerService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/banner")
public class BannerController {

    @Resource
    private BannerService bannerService;

    @GetMapping("/list")
    public Result<?> list() {
        return Result.success(bannerService.list(
                new LambdaQueryWrapper<Banner>().eq(Banner::getStatus, 1).orderByAsc(Banner::getSort)));
    }

    @GetMapping("/listAll")
    public Result<?> listAll() {
        return Result.success(bannerService.list(new LambdaQueryWrapper<Banner>().orderByAsc(Banner::getSort).orderByDesc(Banner::getCreateTime)));
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody Banner banner) {
        bannerService.save(banner);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<?> update(@RequestBody Banner banner) {
        bannerService.updateById(banner);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        bannerService.removeById(id);
        return Result.success();
    }
}
