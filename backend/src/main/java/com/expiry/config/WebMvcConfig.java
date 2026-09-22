package com.expiry.config;

import com.expiry.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.Resource;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${upload.path}")
    private String uploadPath;

    @Resource
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/api/user/login", "/api/user/register",
                        "/api/merchant/login", "/api/merchant/register",
                        "/api/admin/login",
                        "/api/product/list", "/api/product/detail/**",
                        "/api/category/list",
                        "/api/banner/list", "/api/announcement/list",
                        "/api/review/product/**",
                        "/api/merchant/info/public/**",
                        "/api/file/upload",
                        "/uploads/**",
                        "/ws/**"
                );
    }
}
