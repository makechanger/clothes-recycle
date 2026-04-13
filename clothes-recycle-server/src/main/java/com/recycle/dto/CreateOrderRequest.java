package com.recycle.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建回收订单请求 DTO
 * 用户提交预约回收时传入的参数
 */
@Data
public class CreateOrderRequest {

    /** 上门地址ID */
    private Long addressId;

    /** 预约上门日期（格式：yyyy-MM-dd） */
    private String appointmentDate;

    /** 预约时间段-开始（如 09:00） */
    private String timeSlotStart;

    /** 预约时间段-结束（如 11:00） */
    private String timeSlotEnd;

    /** 用户预估重量（kg） */
    private BigDecimal estimatedWeight;

    /** 衣物分类列表（如 ["外套","裤子"]） */
    private List<String> clothesCategories;

    /** 衣物照片URL列表 */
    private List<String> photos;

    /** 用户备注 */
    private String remark;
}
