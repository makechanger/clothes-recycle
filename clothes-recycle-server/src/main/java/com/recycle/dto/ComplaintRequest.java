package com.recycle.dto;

import lombok.Data;

/**
 * 申诉请求 DTO
 */
@Data
public class ComplaintRequest {

    private Long orderId;

    private String type;

    private String description;
}
