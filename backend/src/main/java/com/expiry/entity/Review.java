package com.expiry.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("review")
public class Review {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long productId;
    private Long orderId;
    private Integer rating;
    private String content;
    private String images;
    private String merchantReply;
    private String appendContent;
    private LocalDateTime appendTime;
    private LocalDateTime replyTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
