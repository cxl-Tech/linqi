package com.expiry.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.expiry.common.Result;
import com.expiry.entity.Announcement;
import com.expiry.service.AnnouncementService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/announcement")
public class AnnouncementController {

    @Resource
    private AnnouncementService announcementService;

    @GetMapping("/list")
    public Result<?> list() {
        return Result.success(announcementService.list(
                new LambdaQueryWrapper<Announcement>().eq(Announcement::getStatus, 1)
                        .orderByDesc(Announcement::getCreateTime)));
    }

    @GetMapping("/listAll")
    public Result<?> listAll() {
        return Result.success(announcementService.list(
                new LambdaQueryWrapper<Announcement>().orderByDesc(Announcement::getCreateTime)));
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody Announcement announcement) {
        announcementService.save(announcement);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<?> update(@RequestBody Announcement announcement) {
        announcementService.updateById(announcement);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        announcementService.removeById(id);
        return Result.success();
    }
}
