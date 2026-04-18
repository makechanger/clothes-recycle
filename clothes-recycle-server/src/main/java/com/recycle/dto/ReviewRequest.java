package com.recycle.dto;

import lombok.Data;

/**
 * 评价请求 DTO
 */
@Data
public class ReviewRequest {

    private Long orderId;

    private Integer punctualityScore;

    private Integer attitudeScore;

    private Integer weighingScore;

    private String content;
}
