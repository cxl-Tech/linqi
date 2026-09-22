package com.expiry;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.expiry.mapper")
@EnableScheduling
public class ExpiryFoodApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExpiryFoodApplication.class, args);
    }
}
