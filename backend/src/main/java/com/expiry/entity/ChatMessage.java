package com.expiry.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class ChatMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long senderId;
    private Integer senderType;
    private Long receiverId;
    private Integer receiverType;
    private Long productId;
    private String content;
    private Integer isRead;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
