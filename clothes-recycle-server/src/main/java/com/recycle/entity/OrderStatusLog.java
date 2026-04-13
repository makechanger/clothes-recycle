package com.recycle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单状态流转记录实体类
 * 对应数据库 order_status_log 表，记录每次订单状态变更，用于溯源和审计
 */
@Data
@TableName("order_status_log")
public class OrderStatusLog {

    /** 记录ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单ID */
    private Long orderId;

    /** 变更前状态（首次创建时为NULL） */
    private Integer fromStatus;

    /** 变更后状态 */
    private Integer toStatus;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人类型：user / collector / institution / admin / system */
    private String operatorType;

    /** 操作人位置（可选） */
    private String operatorLocation;

    /** 备注说明 */
    private String remark;

    /** 操作时间 */
    private LocalDateTime createdAt;
}
