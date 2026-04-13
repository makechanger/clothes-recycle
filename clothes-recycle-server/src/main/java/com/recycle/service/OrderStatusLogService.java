package com.recycle.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recycle.entity.OrderStatusLog;
import com.recycle.mapper.OrderStatusLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单状态流转日志服务
 * 每次订单状态变更时调用 log() 方法记录一条日志，用于溯源和审计
 */
@Service
@RequiredArgsConstructor
public class OrderStatusLogService {

    private final OrderStatusLogMapper orderStatusLogMapper;

    /**
     * 记录一条状态变更日志
     *
     * @param orderId      订单ID
     * @param fromStatus   变更前状态（首次创建时传 null）
     * @param toStatus     变更后状态
     * @param operatorId   操作人ID
     * @param operatorType 操作人类型：user / collector / institution / admin / system
     * @param remark       备注说明
     */
    public void log(Long orderId, Integer fromStatus, Integer toStatus,
                    Long operatorId, String operatorType, String remark) {
        OrderStatusLog statusLog = new OrderStatusLog();
        statusLog.setOrderId(orderId);
        statusLog.setFromStatus(fromStatus);
        statusLog.setToStatus(toStatus);
        statusLog.setOperatorId(operatorId);
        statusLog.setOperatorType(operatorType);
        statusLog.setRemark(remark);
        orderStatusLogMapper.insert(statusLog);
    }

    /**
     * 查询某个订单的全部状态流转记录（按时间正序）
     *
     * @param orderId 订单ID
     * @return 状态日志列表
     */
    public List<OrderStatusLog> listByOrderId(Long orderId) {
        return orderStatusLogMapper.selectList(
                new LambdaQueryWrapper<OrderStatusLog>()
                        .eq(OrderStatusLog::getOrderId, orderId)
                        .orderByAsc(OrderStatusLog::getCreatedAt)
        );
    }
}
