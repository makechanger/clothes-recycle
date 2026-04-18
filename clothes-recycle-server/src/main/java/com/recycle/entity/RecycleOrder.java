package com.recycle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 回收订单实体类
 * 对应数据库 recycle_order 表，记录从下单到完成的全部信息
 * 状态值：0=待接单, 1=已接单, 2=上门中, 3=待确认(称重完成), 4=已完成(用户确认), 5=已取消, 6=异常, 7=机构已接收
 */
@Data
@TableName("recycle_order")
public class RecycleOrder {

    /** 订单ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单编号（唯一，格式：RC + 时间戳 + 4位随机数） */
    private String orderNo;

    /** 下单用户ID */
    private Long userId;

    /** 接单回收员ID */
    private Long collectorId;

    /** 接收机构ID */
    private Long institutionId;

    /** 上门地址ID */
    private Long addressId;

    /** 下单时地址快照（JSON），防止地址修改后影响历史订单 */
    private String addressSnapshot;

    /** 预约上门日期 */
    private LocalDate appointmentDate;

    /** 预约时间段-开始（如 09:00） */
    private LocalTime timeSlotStart;

    /** 预约时间段-结束（如 11:00） */
    private LocalTime timeSlotEnd;

    /** 用户预估重量（kg） */
    private BigDecimal estimatedWeight;

    /** 实际称重重量（kg） */
    private BigDecimal actualWeight;

    /** 衣物分类（JSON数组，如 ["外套","裤子"]） */
    private String clothesCategories;

    /** 衣物照片URL列表（JSON数组） */
    private String photos;

    /** 用户备注 */
    private String remark;

    /** 订单状态：0=待接单,1=已接单,2=上门中,3=待确认,4=已完成(用户确认),5=已取消,6=异常,7=机构已接收 */
    private Integer status;

    /** 溯源二维码图片URL */
    private String qrCode;

    /** 本单发放的积分数 */
    private Integer pointsAwarded;

    /** 取消计数日期（用于每日取消上限判断） */
    private LocalDate cancelCountDate;

    /** 取消时间 */
    private LocalDateTime cancelledAt;

    /** 接单时间 */
    private LocalDateTime acceptedAt;

    /** 完成时间 */
    private LocalDateTime completedAt;

    /** 下单时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
