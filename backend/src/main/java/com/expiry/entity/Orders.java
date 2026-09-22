package com.expiry.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Orders {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long merchantId;
    private BigDecimal totalAmount;
    private Integer payStatus;
    private Integer orderStatus;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String expressCompany;
    private String expressNo;
    private Integer logisticsStatus;
    private String remark;
    private LocalDateTime payTime;
    private LocalDateTime shipTime;
    private LocalDateTime receiveTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
