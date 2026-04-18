package com.recycle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分流水实体类
 * 对应数据库 points_transaction 表，记录每一笔积分变动
 * 变动类型：EARN-获取（回收订单）、EXCHANGE-兑换（积分商城）、DEDUCT-扣除、ADJUST-调整
 */
@Data
@TableName("points_transaction")
public class PointsTransaction {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID，关联 user 表 */
    private Long userId;

    /** 变动类型：EARN-获取 EXCHANGE-兑换 DEDUCT-扣除 ADJUST-调整 */
    private String type;

    /** 积分变动数量（正数表示增加，负数表示减少） */
    private Integer amount;

    /** 变动后的积分余额 */
    private Integer balanceAfter;

    /** 关联的回收订单ID（获取积分时记录） */
    private Long relatedOrderId;

    /** 关联的兑换订单ID（兑换积分时记录） */
    private Long relatedExchangeId;

    /** 变动描述 */
    private String description;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
