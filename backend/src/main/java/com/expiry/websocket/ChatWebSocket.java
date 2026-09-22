package com.expiry.websocket;

import com.expiry.entity.ChatMessage;
import com.expiry.service.ChatMessageService;
import com.expiry.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint("/ws/chat")
public class ChatWebSocket {

    private static ChatMessageService chatMessageService;
    private static JwtUtils jwtUtils;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 连接池: key = "type_id" (如 "1_3" 表示消费者ID=3)
    private static final ConcurrentHashMap<String, Session> onlineSessions = new ConcurrentHashMap<>();

    private Session session;
    private Long userId;
    private Integer userType;
    private String sessionKey;

    @Autowired
    public void setChatMessageService(ChatMessageService service) {
        ChatWebSocket.chatMessageService = service;
    }

    @Autowired
    public void setJwtUtils(JwtUtils utils) {
        ChatWebSocket.jwtUtils = utils;
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        // 从 URL 参数获取 token
        Map<String, java.util.List<String>> params = session.getRequestParameterMap();
        String token = null;
        if (params.containsKey("token")) {
            token = params.get("token").get(0);
        }
        if (token == null || token.isEmpty()) {
            try { session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "缺少认证信息")); } catch (IOException e) {}
            return;
        }
        try {
            if (jwtUtils.isTokenExpired(token)) {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "登录已过期"));
                return;
            }
            Claims claims = jwtUtils.parseToken(token);
            this.userId = Long.valueOf(claims.get("id").toString());
            String role = claims.get("role").toString();
            this.userType = "merchant".equals(role) ? 2 : 1;
            this.sessionKey = this.userType + "_" + this.userId;
            onlineSessions.put(sessionKey, session);
            log.info("WebSocket连接成功: {} ({})", sessionKey, role);
        } catch (Exception e) {
            log.warn("WebSocket认证失败: {}", e.getMessage());
            try { session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "认证失败")); } catch (IOException ex) {}
        }
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            Map<String, Object> data = objectMapper.readValue(message, Map.class);
            Long receiverId = Long.valueOf(data.get("receiverId").toString());
            Integer receiverType = Integer.valueOf(data.get("receiverType").toString());
            String content = data.get("content").toString();
            Long productId = data.containsKey("productId") && data.get("productId") != null
                    ? Long.valueOf(data.get("productId").toString()) : null;

            // 保存消息到数据库
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSenderId(this.userId);
            chatMessage.setSenderType(this.userType);
            chatMessage.setReceiverId(receiverId);
            chatMessage.setReceiverType(receiverType);
            chatMessage.setProductId(productId);
            chatMessage.setContent(content);
            chatMessage.setIsRead(0);
            chatMessageService.save(chatMessage);

            // 构建发送给客户端的消息
            Map<String, Object> outMsg = new java.util.HashMap<>();
            outMsg.put("id", chatMessage.getId());
            outMsg.put("senderId", this.userId);
            outMsg.put("senderType", this.userType);
            outMsg.put("receiverId", receiverId);
            outMsg.put("receiverType", receiverType);
            outMsg.put("content", content);
            outMsg.put("productId", productId);
            outMsg.put("createTime", java.time.LocalDateTime.now().toString());
            String outJson = objectMapper.writeValueAsString(outMsg);

            // 实时推送给对方
            String targetKey = receiverType + "_" + receiverId;
            Session targetSession = onlineSessions.get(targetKey);
            if (targetSession != null && targetSession.isOpen()) {
                targetSession.getBasicRemote().sendText(outJson);
            }

            // 也回传给发送者确认
            if (this.session != null && this.session.isOpen()) {
                this.session.getBasicRemote().sendText(outJson);
            }
        } catch (Exception e) {
            log.error("WebSocket消息处理异常: {}", e.getMessage());
        }
    }

    /**
     * 广播撤回消息给双方
     */
    public static void broadcastRecall(Long msgId, Long receiverId, Integer receiverType, Long senderId, Integer senderType) {
        try {
            Map<String, Object> recallMsg = new java.util.HashMap<>();
            recallMsg.put("type", "recall");
            recallMsg.put("msgId", msgId);
            String json = objectMapper.writeValueAsString(recallMsg);
            // 通知接收方
            Session receiverSession = onlineSessions.get(receiverType + "_" + receiverId);
            if (receiverSession != null && receiverSession.isOpen()) {
                receiverSession.getBasicRemote().sendText(json);
            }
            // 通知发送方（可能多设备）
            Session senderSession = onlineSessions.get(senderType + "_" + senderId);
            if (senderSession != null && senderSession.isOpen()) {
                senderSession.getBasicRemote().sendText(json);
            }
        } catch (Exception e) {
            log.error("广播撤回消息异常: {}", e.getMessage());
        }
    }

    @OnClose
    public void onClose() {
        if (sessionKey != null) {
            onlineSessions.remove(sessionKey);
            log.info("WebSocket连接关闭: {}", sessionKey);
        }
    }

    @OnError
    public void onError(Throwable error) {
        log.error("WebSocket错误: {}", error.getMessage());
    }
}
