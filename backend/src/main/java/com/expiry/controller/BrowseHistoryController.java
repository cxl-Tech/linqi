package com.expiry.controller;

import com.expiry.common.Result;
import com.expiry.service.BrowseHistoryService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/history")
public class BrowseHistoryController {

    @Resource
    private BrowseHistoryService browseHistoryService;

    @PostMapping("/record/{productId}")
    public Result<?> record(@PathVariable Long productId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        browseHistoryService.record(userId, productId);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<?> list(@RequestParam(defaultValue = "1") int current,
                          @RequestParam(defaultValue = "10") int size,
                          HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(browseHistoryService.pageByUser(userId, current, size));
    }
}
