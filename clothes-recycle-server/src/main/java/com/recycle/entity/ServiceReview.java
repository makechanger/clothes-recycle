package com.recycle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 服务评价实体
 */
@Data
@TableName("service_review")
public class ServiceReview {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long userId;

    private Long collectorId;

    private Integer punctualityScore;

    private Integer attitudeScore;

    private Integer weighingScore;

    private String content;

    private LocalDateTime createdAt;
}
