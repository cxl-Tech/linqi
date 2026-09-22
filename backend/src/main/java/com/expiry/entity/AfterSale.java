package com.expiry.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("after_sale")
public class AfterSale {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long userId;
    private Integer type;
    private String reason;
    private String description;
    private String images;
    private Integer status;
    private String merchantReply;
    private String adminReply;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
