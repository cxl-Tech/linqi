package com.expiry.interceptor;

import com.expiry.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Resource
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token == null || token.isEmpty()) {
            token = request.getHeader("token");
        }
        if (token == null || token.isEmpty()) {
            sendError(response, 401, "请先登录");
            return false;
        }
        try {
            if (jwtUtils.isTokenExpired(token)) {
                sendError(response, 401, "登录已过期，请重新登录");
                return false;
            }
            Claims claims = jwtUtils.parseToken(token);
            request.setAttribute("userId", Long.valueOf(claims.get("id").toString()));
            request.setAttribute("username", claims.get("username").toString());
            request.setAttribute("role", claims.get("role").toString());
            return true;
        } catch (Exception e) {
            log.warn("Token验证失败: {}", e.getMessage());
            sendError(response, 401, "登录信息无效，请重新登录");
            return false;
        }
    }

    private void sendError(HttpServletResponse response, int code, String message) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(200);
        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("message", message);
        response.getWriter().write(new ObjectMapper().writeValueAsString(result));
    }
}
