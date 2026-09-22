package com.expiry.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.expiry.entity.ChatMessage;
import com.expiry.mapper.ChatMessageMapper;
import org.springframework.stereotype.Service;

import com.expiry.entity.User;
import com.expiry.entity.Merchant;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatMessageService extends ServiceImpl<ChatMessageMapper, ChatMessage> {

    @Resource
    private UserService userService;

    @Resource
    private MerchantService merchantService;

    /**
     * 获取两方之间的聊天记录（分页）
     */
    public Page<ChatMessage> getHistory(Long userId, Integer userType, Long targetId, Integer targetType, int current, int size) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                .and(a -> a.eq(ChatMessage::getSenderId, userId).eq(ChatMessage::getSenderType, userType)
                        .eq(ChatMessage::getReceiverId, targetId).eq(ChatMessage::getReceiverType, targetType))
                .or(b -> b.eq(ChatMessage::getSenderId, targetId).eq(ChatMessage::getSenderType, targetType)
                        .eq(ChatMessage::getReceiverId, userId).eq(ChatMessage::getReceiverType, userType))
        );
        wrapper.orderByAsc(ChatMessage::getCreateTime);
        return this.page(new Page<>(current, size), wrapper);
    }

    /**
     * 获取用户的会话列表（每个对话的最后一条消息）
     */
    public List<Map<String, Object>> getConversations(Long userId, Integer userType) {
        // 查所有与该用户相关的消息
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                .and(a -> a.eq(ChatMessage::getSenderId, userId).eq(ChatMessage::getSenderType, userType))
                .or(b -> b.eq(ChatMessage::getReceiverId, userId).eq(ChatMessage::getReceiverType, userType))
        );
        wrapper.orderByDesc(ChatMessage::getCreateTime);
        List<ChatMessage> allMessages = this.list(wrapper);

        // 按对方分组，取最新消息
        Map<String, ChatMessage> latestMap = new LinkedHashMap<>();
        Map<String, Long> unreadMap = new HashMap<>();

        for (ChatMessage msg : allMessages) {
            String key;
            Long targetId;
            Integer targetTypeVal;
            if (msg.getSenderId().equals(userId) && msg.getSenderType().equals(userType)) {
                targetId = msg.getReceiverId();
                targetTypeVal = msg.getReceiverType();
            } else {
                targetId = msg.getSenderId();
                targetTypeVal = msg.getSenderType();
            }
            key = targetId + "_" + targetTypeVal;

            if (!latestMap.containsKey(key)) {
                latestMap.put(key, msg);
            }
            // 统计未读数（对方发给我的未读消息）
            if (msg.getReceiverId().equals(userId) && msg.getReceiverType().equals(userType) && msg.getIsRead() == 0) {
                unreadMap.merge(key, 1L, Long::sum);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, ChatMessage> entry : latestMap.entrySet()) {
            String[] parts = entry.getKey().split("_");
            Long targetId = Long.valueOf(parts[0]);
            Integer targetTypeVal = Integer.valueOf(parts[1]);
            Map<String, Object> item = new HashMap<>();
            item.put("targetId", targetId);
            item.put("targetType", targetTypeVal);
            item.put("lastMessage", entry.getValue().getContent());
            item.put("lastTime", entry.getValue().getCreateTime());
            item.put("unreadCount", unreadMap.getOrDefault(entry.getKey(), 0L));
            // 填充对方昵称和头像
            if (targetTypeVal == 1) {
                User u = userService.getById(targetId);
                if (u != null) {
                    item.put("targetName", u.getNickname() != null ? u.getNickname() : u.getUsername());
                    item.put("targetAvatar", u.getAvatar());
                }
            } else if (targetTypeVal == 2) {
                Merchant m = merchantService.getById(targetId);
                if (m != null) {
                    item.put("targetName", m.getShopName() != null ? m.getShopName() : m.getUsername());
                    item.put("targetAvatar", m.getShopLogo());
                }
            }
            result.add(item);
        }
        return result;
    }

    /**
     * 获取未读消息总数
     */
    public long getUnreadCount(Long userId, Integer userType) {
        return this.count(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getReceiverId, userId)
                .eq(ChatMessage::getReceiverType, userType)
                .eq(ChatMessage::getIsRead, 0));
    }

    /**
     * 标记对话消息为已读
     */
    public void markRead(Long userId, Integer userType, Long senderId, Integer senderType) {
        this.update(new LambdaUpdateWrapper<ChatMessage>()
                .eq(ChatMessage::getReceiverId, userId)
                .eq(ChatMessage::getReceiverType, userType)
                .eq(ChatMessage::getSenderId, senderId)
                .eq(ChatMessage::getSenderType, senderType)
                .eq(ChatMessage::getIsRead, 0)
                .set(ChatMessage::getIsRead, 1));
    }

    /**
     * 删除会话（双向消息全部删除）
     */
    public void deleteConversation(Long userId, Integer userType, Long targetId, Integer targetType) {
        this.remove(new LambdaQueryWrapper<ChatMessage>()
                .and(w -> w
                        .and(a -> a.eq(ChatMessage::getSenderId, userId).eq(ChatMessage::getSenderType, userType)
                                .eq(ChatMessage::getReceiverId, targetId).eq(ChatMessage::getReceiverType, targetType))
                        .or(b -> b.eq(ChatMessage::getSenderId, targetId).eq(ChatMessage::getSenderType, targetType)
                                .eq(ChatMessage::getReceiverId, userId).eq(ChatMessage::getReceiverType, userType))
                ));
    }
}
