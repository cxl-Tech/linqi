package com.expiry.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("merchant")
public class Merchant {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String shopName;
    private String shopLogo;
    private String contact;
    private String phone;
    private String email;
    private String description;
    private String licenseImg;
    private Integer status;
    private Integer auditStatus;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
