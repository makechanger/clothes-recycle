package com.recycle.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.recycle.common.BusinessException;
import com.recycle.entity.RecycleOrder;
import com.recycle.entity.User;
import com.recycle.mapper.RecycleOrderMapper;
import com.recycle.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 回收员订单服务
 * 处理回收员端的订单操作：查看待接单、接单、开始上门、完成称重
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollectorOrderService {

    private final RecycleOrderMapper recycleOrderMapper;
    private final UserMapper userMapper;
    private final OrderStatusLogService orderStatusLogService;

    /**
     * 校验当前用户是否为回收员角色
     * 非回收员调用回收员接口时抛出异常
     *
     * @param userId 当前用户ID
     */
    private void checkCollectorRole(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || !"COLLECTOR".equals(user.getRole())) {
            throw new BusinessException(403, "无权限，仅回收员可操作");
        }
    }

    /**
     * 查询待接单列表
     * 返回所有 status=0（待接单）的订单，按创建时间倒序
     *
     * @param userId 当前回收员ID（用于角色校验）
     * @return 待接单订单列表
     */
    public List<RecycleOrder> pendingList(Long userId) {
        checkCollectorRole(userId);

        return recycleOrderMapper.selectList(
                new LambdaQueryWrapper<RecycleOrder>()
                        .eq(RecycleOrder::getStatus, 0)
                        .orderByDesc(RecycleOrder::getCreatedAt)
        );
    }

    /**
     * 查询回收员自己的订单列表
     * 支持按状态筛选，按创建时间倒序
     *
     * @param userId 回收员ID
     * @param status 订单状态（null 表示查全部）
     * @return 订单列表
     */
    public List<RecycleOrder> myOrders(Long userId, Integer status) {
        checkCollectorRole(userId);

        LambdaQueryWrapper<RecycleOrder> wrapper = new LambdaQueryWrapper<RecycleOrder>()
                .eq(RecycleOrder::getCollectorId, userId)
                .orderByDesc(RecycleOrder::getCreatedAt);

        if (status != null) {
            wrapper.eq(RecycleOrder::getStatus, status);
        }

        return recycleOrderMapper.selectList(wrapper);
    }

    /**
     * 接单
     * 使用乐观更新防止并发接单：UPDATE WHERE status=0
     * 只有 status=0（待接单）的订单可以被接
     *
     * @param orderId 订单ID
     * @param userId  回收员ID
     */
    @Transactional
    public void acceptOrder(Long orderId, Long userId) {
        checkCollectorRole(userId);

        // 乐观更新：只有 status=0 才能接单，防止多个回收员同时接同一单
        int rows = recycleOrderMapper.update(null,
                new LambdaUpdateWrapper<RecycleOrder>()
                        .eq(RecycleOrder::getId, orderId)
                        .eq(RecycleOrder::getStatus, 0)
                        .set(RecycleOrder::getCollectorId, userId)
                        .set(RecycleOrder::getStatus, 1)
                        .set(RecycleOrder::getAcceptedAt, LocalDateTime.now())
        );

        if (rows == 0) {
            throw new BusinessException(400, "接单失败，订单已被接或状态不正确");
        }

        // 记录状态日志
        orderStatusLogService.log(orderId, 0, 1, userId, "collector", "回收员接单");

        log.info("回收员 {} 接单 {}", userId, orderId);
    }

    /**
     * 开始上门（出发）
     * 仅 status=1（已接单）且是自己接的单才能操作
     *
     * @param orderId 订单ID
     * @param userId  回收员ID
     */
    @Transactional
    public void startPickup(Long orderId, Long userId) {
        checkCollectorRole(userId);

        RecycleOrder order = getMyOrder(orderId, userId);

        if (order.getStatus() != 1) {
            throw new BusinessException(400, "当前订单状态不可出发");
        }

        recycleOrderMapper.update(null,
                new LambdaUpdateWrapper<RecycleOrder>()
                        .eq(RecycleOrder::getId, orderId)
                        .set(RecycleOrder::getStatus, 2)
        );

        // 记录状态日志
        orderStatusLogService.log(orderId, 1, 2, userId, "collector", "回收员出发上门");

        log.info("回收员 {} 出发上门，订单 {}", userId, orderId);
    }

    /**
     * 完成称重
     * 仅 status=2（上门中）且是自己接的单才能操作
     * 填写实际重量后，订单进入待确认状态，等待用户确认
     *
     * @param orderId      订单ID
     * @param userId       回收员ID
     * @param actualWeight 实际称重重量（kg）
     */
    @Transactional
    public void completeWeighing(Long orderId, Long userId, BigDecimal actualWeight) {
        checkCollectorRole(userId);

        if (actualWeight == null || actualWeight.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(400, "请填写有效的称重重量");
        }

        RecycleOrder order = getMyOrder(orderId, userId);

        if (order.getStatus() != 2) {
            throw new BusinessException(400, "当前订单状态不可称重");
        }

        recycleOrderMapper.update(null,
                new LambdaUpdateWrapper<RecycleOrder>()
                        .eq(RecycleOrder::getId, orderId)
                        .set(RecycleOrder::getStatus, 3)
                        .set(RecycleOrder::getActualWeight, actualWeight)
        );

        // 记录状态日志
        orderStatusLogService.log(orderId, 2, 3, userId, "collector",
                "称重完成，实际重量" + actualWeight + "kg，等待用户确认");

        log.info("回收员 {} 完成称重，订单 {}，重量 {}kg", userId, orderId, actualWeight);
    }

    /**
     * 查询订单详情（回收员视角）
     * 待接单(status=0)的订单所有回收员都可查看；
     * 其他状态的订单只有接单的回收员本人可查看
     *
     * @param orderId 订单ID
     * @param userId  回收员ID
     * @return 订单详情
     */
    public RecycleOrder getOrderDetail(Long orderId, Long userId) {
        checkCollectorRole(userId);

        RecycleOrder order = recycleOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        // 待接单的订单所有回收员都可查看（用于决定是否接单）
        if (order.getStatus() == 0) {
            return order;
        }

        // 其他状态只能查看自己接的订单
        if (!userId.equals(order.getCollectorId())) {
            throw new BusinessException(404, "订单不存在或非本人接单");
        }
        return order;
    }

    /**
     * 查询回收员自己接的某个订单
     * 包含归属校验，只能操作自己接的订单
     *
     * @param orderId 订单ID
     * @param userId  回收员ID
     * @return 订单详情
     */
    public RecycleOrder getMyOrder(Long orderId, Long userId) {
        RecycleOrder order = recycleOrderMapper.selectById(orderId);
        if (order == null || !userId.equals(order.getCollectorId())) {
            throw new BusinessException(404, "订单不存在或非本人接单");
        }
        return order;
    }
}
