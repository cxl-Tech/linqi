package com.expiry.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("browse_history")
public class BrowseHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long productId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
