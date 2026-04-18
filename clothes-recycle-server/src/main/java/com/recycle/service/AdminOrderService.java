package com.recycle.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recycle.common.BusinessException;
import com.recycle.entity.RecycleOrder;
import com.recycle.entity.User;
import com.recycle.mapper.RecycleOrderMapper;
import com.recycle.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理员订单查询服务
 * 提供管理员查看所有订单（分页+筛选）和订单详情功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final RecycleOrderMapper recycleOrderMapper;
    private final UserMapper userMapper;
    private final OrderStatusLogService orderStatusLogService;

    /**
     * 分页查询所有订单
     * 支持按状态筛选、按订单号模糊搜索
     *
     * @param status  订单状态（null 表示全部）
     * @param orderNo 订单号关键字（null 表示不筛选）
     * @param page    页码（从1开始）
     * @param size    每页条数
     * @return 分页结果，records 中每条订单附带用户手机号和姓名
     */
    public Map<String, Object> listOrders(Integer status, String orderNo, Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;

        // 1. 构建查询条件
        LambdaQueryWrapper<RecycleOrder> wrapper = new LambdaQueryWrapper<RecycleOrder>()
                .eq(status != null, RecycleOrder::getStatus, status)
                .like(orderNo != null && !orderNo.isEmpty(), RecycleOrder::getOrderNo, orderNo)
                .orderByDesc(RecycleOrder::getCreatedAt);

        // 2. 分页查询
        IPage<RecycleOrder> pageResult = recycleOrderMapper.selectPage(
                new Page<>(page, size), wrapper
        );

        // 3. 批量查询关联用户信息（手机号、姓名）
        List<RecycleOrder> orders = pageResult.getRecords();
        Map<Long, User> userMap = batchQueryUsers(orders);

        // 4. 组装返回数据
        List<Map<String, Object>> records = new ArrayList<>();
        for (RecycleOrder order : orders) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", order.getId());
            item.put("orderNo", order.getOrderNo());
            item.put("userId", order.getUserId());
            item.put("status", order.getStatus());
            item.put("clothesCategories", order.getClothesCategories());
            item.put("estimatedWeight", order.getEstimatedWeight());
            item.put("actualWeight", order.getActualWeight());
            item.put("pointsAwarded", order.getPointsAwarded());
            item.put("appointmentDate", order.getAppointmentDate());
            item.put("createdAt", order.getCreatedAt());

            User user = userMap.get(order.getUserId());
            if (user != null) {
                item.put("userPhone", user.getPhone());
                item.put("userName", user.getName());
            }
            records.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", pageResult.getTotal());
        result.put("page", pageResult.getCurrent());
        result.put("size", pageResult.getSize());
        return result;
    }

    /**
     * 查询订单详情（管理员视角，无归属校验）
     * 包含订单完整信息、用户信息、状态流转日志
     *
     * @param orderId 订单ID
     * @return 订单详情
     */
    public Map<String, Object> getOrderDetail(Long orderId) {
        RecycleOrder order = recycleOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        // 查询下单用户信息
        User user = userMapper.selectById(order.getUserId());

        // 查询状态流转日志
        var statusLogs = orderStatusLogService.listByOrderId(orderId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", order.getId());
        result.put("orderNo", order.getOrderNo());
        result.put("userId", order.getUserId());
        result.put("collectorId", order.getCollectorId());
        result.put("institutionId", order.getInstitutionId());
        result.put("status", order.getStatus());
        result.put("addressSnapshot", order.getAddressSnapshot());
        result.put("appointmentDate", order.getAppointmentDate());
        result.put("timeSlotStart", order.getTimeSlotStart());
        result.put("timeSlotEnd", order.getTimeSlotEnd());
        result.put("clothesCategories", order.getClothesCategories());
        result.put("photos", order.getPhotos());
        result.put("estimatedWeight", order.getEstimatedWeight());
        result.put("actualWeight", order.getActualWeight());
        result.put("pointsAwarded", order.getPointsAwarded());
        result.put("remark", order.getRemark());
        result.put("qrCode", order.getQrCode());
        result.put("createdAt", order.getCreatedAt());
        result.put("acceptedAt", order.getAcceptedAt());
        result.put("completedAt", order.getCompletedAt());
        result.put("cancelledAt", order.getCancelledAt());

        if (user != null) {
            result.put("userPhone", user.getPhone());
            result.put("userName", user.getName());
        }

        result.put("statusLogs", statusLogs);
        return result;
    }

    /**
     * 批量查询订单关联的用户信息
     */
    private Map<Long, User> batchQueryUsers(List<RecycleOrder> orders) {
        if (orders.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> userIds = orders.stream()
                .map(RecycleOrder::getUserId)
                .distinct()
                .collect(Collectors.toList());
        List<User> users = userMapper.selectBatchIds(userIds);
        return users.stream().collect(Collectors.toMap(User::getId, u -> u));
    }
}
