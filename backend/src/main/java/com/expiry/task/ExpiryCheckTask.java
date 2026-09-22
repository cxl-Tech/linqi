package com.expiry.task;

import com.expiry.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Slf4j
@Component
public class ExpiryCheckTask {

    @Resource
    private ProductService productService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void checkExpiryStatus() {
        log.info("========== 开始执行临期状态检测任务 ==========");
        productService.updateExpiryStatus();
        log.info("========== 临期状态检测任务执行完成 ==========");
    }
}
