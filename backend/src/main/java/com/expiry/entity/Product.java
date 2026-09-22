package com.expiry.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long merchantId;
    private Long categoryId;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private Integer stockWarning;
    private LocalDate productionDate;
    private Integer shelfLifeDays;
    private LocalDate expiryDate;
    private Integer expiryStatus;
    private String images;
    private String manufacturer;
    private String origin;
    private String licenseNo;
    private String qualityReportImgs;
    private Integer status;
    private Integer auditStatus;
    private Integer sales;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
