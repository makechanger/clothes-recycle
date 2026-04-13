package com.recycle.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 回收员完成称重请求 DTO
 * 回收员上门回收后，填写实际称重重量
 */
@Data
public class CompleteWeighingRequest {

    /** 实际称重重量（kg） */
    private BigDecimal actualWeight;
}
