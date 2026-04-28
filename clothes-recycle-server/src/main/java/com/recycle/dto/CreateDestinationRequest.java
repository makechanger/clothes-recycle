package com.recycle.dto;

import lombok.Data;

/**
 * 分配衣物去向请求（单订单）
 */
@Data
public class CreateDestinationRequest {

    /** 订单ID */
    private Long orderId;

    /** 去向类型：DONATION / RECYCLE / ENVIRONMENTAL */
    private String destinationType;

    /** 去向描述（如"捐赠至云南山区小学"） */
    private String description;
}
