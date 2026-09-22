package com.expiry.controller;

import com.expiry.common.Result;
import com.expiry.entity.ChatMessage;
import com.expiry.service.ChatMessageService;
import com.expiry.websocket.ChatWebSocket;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Resource
    private ChatMessageService chatMessageService;

    /**
     * 获取与某用户的聊天历史
     */
    @GetMapping("/history")
    public Result<?> history(@RequestParam Long targetId,
                             @RequestParam Integer targetType,
                             @RequestParam(defaultValue = "1") int current,
                             @RequestParam(defaultValue = "50") int size,
                             HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        Integer userType = "merchant".equals(role) ? 2 : 1;
        return Result.success(chatMessageService.getHistory(userId, userType, targetId, targetType, current, size));
    }

    /**
     * 获取会话列表
     */
    @GetMapping("/conversations")
    public Result<?> conversations(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        Integer userType = "merchant".equals(role) ? 2 : 1;
        List<Map<String, Object>> list = chatMessageService.getConversations(userId, userType);
        return Result.success(list);
    }

    /**
     * 获取未读消息数
     */
    @GetMapping("/unread")
    public Result<?> unreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        Integer userType = "merchant".equals(role) ? 2 : 1;
        return Result.success(chatMessageService.getUnreadCount(userId, userType));
    }

    /**
     * 标记某对话为已读
     */
    @PutMapping("/read")
    public Result<?> markRead(@RequestParam Long senderId,
                              @RequestParam Integer senderType,
                              HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        Integer userType = "merchant".equals(role) ? 2 : 1;
        chatMessageService.markRead(userId, userType, senderId, senderType);
        return Result.success();
    }

    /**
     * 撤回消息（2分钟内）
     */
    @DeleteMapping("/recall/{id}")
    public Result<?> recall(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        Integer userType = "merchant".equals(role) ? 2 : 1;
        ChatMessage msg = chatMessageService.getById(id);
        if (msg == null) return Result.error("消息不存在");
        if (!msg.getSenderId().equals(userId) || !msg.getSenderType().equals(userType)) {
            return Result.error("只能撤回自己的消息");
        }
        if (msg.getCreateTime() != null && Duration.between(msg.getCreateTime(), LocalDateTime.now()).toMinutes() >= 2) {
            return Result.error("超过2分钟无法撤回");
        }
        chatMessageService.removeById(id);
        // 通过 WebSocket 通知对方撤回
        ChatWebSocket.broadcastRecall(id, msg.getReceiverId(), msg.getReceiverType(), msg.getSenderId(), msg.getSenderType());
        return Result.success();
    }

    /**
     * 删除会话（删除与某用户的所有消息）
     */
    @DeleteMapping("/conversation")
    public Result<?> deleteConversation(@RequestParam Long targetId,
                                        @RequestParam Integer targetType,
                                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        Integer userType = "merchant".equals(role) ? 2 : 1;
        chatMessageService.deleteConversation(userId, userType, targetId, targetType);
        return Result.success();
    }
}
