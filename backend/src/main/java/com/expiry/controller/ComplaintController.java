package com.expiry.controller;

import com.expiry.common.Result;
import com.expiry.entity.Complaint;
import com.expiry.service.ComplaintService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/complaint")
public class ComplaintController {

    @Resource
    private ComplaintService complaintService;

    @PostMapping("/add")
    public Result<?> add(@RequestBody Complaint complaint, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        complaint.setUserId(userId);
        complaint.setStatus(0);
        complaintService.save(complaint);
        return Result.success("投诉已提交");
    }

    @GetMapping("/user/list")
    public Result<?> userList(@RequestParam(defaultValue = "1") int current,
                              @RequestParam(defaultValue = "10") int size,
                              HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(complaintService.pageByUser(userId, current, size));
    }
}
