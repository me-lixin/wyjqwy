package com.wyjqwy.server.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("template")
public class TemplateEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer type;
    private BigDecimal amount;
    private Long categoryId;
    private String note;
    private Integer sort;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
