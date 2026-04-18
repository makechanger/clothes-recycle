package com.recycle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 异常申诉实体
 */
@Data
@TableName("complaint")
public class Complaint {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long userId;

    private String type;

    private String description;

    private Integer status;

    private String adminRemark;

    private LocalDateTime createdAt;

    private LocalDateTime handledAt;

    private String action;

    private Integer refundAmount;
}
