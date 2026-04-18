package com.recycle.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recycle.common.BusinessException;
import com.recycle.dto.CreateOrderRequest;
import com.recycle.entity.RecycleOrder;
import com.recycle.entity.UserAddress;
import com.recycle.mapper.RecycleOrderMapper;
import com.recycle.mapper.UserAddressMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 回收订单服务
 * 处理用户端的订单操作：创建、查看列表、查看详情、取消、确认完成
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecycleOrderService {

    private final RecycleOrderMapper recycleOrderMapper;
    private final UserAddressMapper userAddressMapper;
    private final OrderStatusLogService orderStatusLogService;
    private final ObjectMapper objectMapper;

    /**
     * 创建回收订单
     * 1. 校验地址归属
     * 2. 生成订单号（RC + 时间戳 + 4位随机数）
     * 3. 将地址快照为 JSON（防止地址修改影响历史订单）
     * 4. 记录状态日志
     *
     * @param userId  当前用户ID
     * @param request 创建订单请求参数
     * @return 创建的订单
     */
    @Transactional
    public RecycleOrder createOrder(Long userId, CreateOrderRequest request) {
        // 1. 校验必填参数
        if (request.getAddressId() == null) {
            throw new BusinessException(400, "请选择上门地址");
        }
        if (request.getAppointmentDate() == null || request.getAppointmentDate().isEmpty()) {
            throw new BusinessException(400, "请选择预约日期");
        }
        if (request.getTimeSlotStart() == null || request.getTimeSlotEnd() == null) {
            throw new BusinessException(400, "请选择预约时间段");
        }

        // 2. 校验地址归属（只能使用自己的地址）
        UserAddress address = userAddressMapper.selectById(request.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(400, "地址不存在");
        }

        // 3. 生成订单号：RC + yyyyMMddHHmmss + 4位随机数
        String orderNo = generateOrderNo();

        // 4. 地址快照序列化为 JSON
        String addressSnapshot = serializeAddress(address);

        // 5. 衣物分类和照片序列化为 JSON 字符串
        String categoriesJson = toJson(request.getClothesCategories());
        String photosJson = toJson(request.getPhotos());

        // 6. 构建订单实体
        RecycleOrder order = new RecycleOrder();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setAddressId(request.getAddressId());
        order.setAddressSnapshot(addressSnapshot);
        order.setAppointmentDate(LocalDate.parse(request.getAppointmentDate(), DateTimeFormatter.ISO_LOCAL_DATE));
        order.setTimeSlotStart(LocalTime.parse(request.getTimeSlotStart()));
        order.setTimeSlotEnd(LocalTime.parse(request.getTimeSlotEnd()));
        order.setEstimatedWeight(request.getEstimatedWeight());
        order.setClothesCategories(categoriesJson);
        order.setPhotos(photosJson);
        order.setRemark(request.getRemark());
        order.setStatus(0); // 待接单

        recycleOrderMapper.insert(order);

        // 7. 记录状态日志：创建订单
        orderStatusLogService.log(order.getId(), null, 0, userId, "user", "用户创建订单");

        log.info("用户 {} 创建订单 {}", userId, orderNo);
        return order;
    }

    /**
     * 查询用户的订单列表
     * 支持按状态筛选，按创建时间倒序排列
     *
     * @param userId 用户ID
     * @param status 订单状态（null 表示查全部）
     * @return 订单列表
     */
    public List<RecycleOrder> listByUser(Long userId, Integer status) {
        LambdaQueryWrapper<RecycleOrder> wrapper = new LambdaQueryWrapper<RecycleOrder>()
                .eq(RecycleOrder::getUserId, userId)
                .orderByDesc(RecycleOrder::getCreatedAt);

        // 如果指定了状态则按状态筛选
        if (status != null) {
            wrapper.eq(RecycleOrder::getStatus, status);
        }

        return recycleOrderMapper.selectList(wrapper);
    }

    /**
     * 查询订单详情
     * 包含归属校验，用户只能查看自己的订单
     *
     * @param orderId 订单ID
     * @param userId  当前用户ID
     * @return 订单详情
     */
    public RecycleOrder getDetail(Long orderId, Long userId) {
        RecycleOrder order = recycleOrderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(404, "订单不存在");
        }
        return order;
    }

    /**
     * 用户取消订单
     * 仅 status=0（待接单）或 status=1（已接单）可取消
     *
     * @param orderId 订单ID
     * @param userId  当前用户ID
     */
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        RecycleOrder order = getDetail(orderId, userId);

        // 只有待接单(0)和已接单(1)状态可以取消
        if (order.getStatus() != 0 && order.getStatus() != 1) {
            throw new BusinessException(400, "当前订单状态不可取消");
        }

        // 更新订单状态为已取消
        int oldStatus = order.getStatus();
        LambdaUpdateWrapper<RecycleOrder> wrapper = new LambdaUpdateWrapper<RecycleOrder>()
                .eq(RecycleOrder::getId, orderId)
                .set(RecycleOrder::getStatus, 5)
                .set(RecycleOrder::getCancelledAt, java.time.LocalDateTime.now());
        recycleOrderMapper.update(null, wrapper);

        // 记录状态日志
        orderStatusLogService.log(orderId, oldStatus, 5, userId, "user", "用户取消订单");

        log.info("用户 {} 取消订单 {}", userId, order.getOrderNo());
    }

    /**
     * 用户确认完成订单
     * 仅 status=3（待确认/称重完成）可确认
     * 确认后等待机构扫码接收，届时再发放积分
     *
     * @param orderId 订单ID
     * @param userId  当前用户ID
     */
    @Transactional
    public void confirmOrder(Long orderId, Long userId) {
        RecycleOrder order = getDetail(orderId, userId);

        // 只有待确认(3)状态可以确认完成
        if (order.getStatus() != 3) {
            throw new BusinessException(400, "当前订单状态不可确认");
        }

        // 更新订单状态为已完成（用户确认），等待机构扫码接收后再发放积分
        LambdaUpdateWrapper<RecycleOrder> wrapper = new LambdaUpdateWrapper<RecycleOrder>()
                .eq(RecycleOrder::getId, orderId)
                .set(RecycleOrder::getStatus, 4)
                .set(RecycleOrder::getCompletedAt, java.time.LocalDateTime.now());
        recycleOrderMapper.update(null, wrapper);

        // 记录状态日志
        orderStatusLogService.log(orderId, 3, 4, userId, "user", "用户确认完成，等待机构接收");

        log.info("用户 {} 确认订单 {} 完成", userId, order.getOrderNo());
    }

    // ==================== 私有工具方法 ====================

    /**
     * 生成订单号：RC + yyyyMMddHHmmss + 4位随机数
     */
    private String generateOrderNo() {
        String timestamp = java.time.LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "RC" + timestamp + random;
    }

    /**
     * 将地址信息序列化为 JSON 快照
     */
    private String serializeAddress(UserAddress address) {
        try {
            Map<String, Object> snapshot = new HashMap<>();
            snapshot.put("name", address.getName());
            snapshot.put("phone", address.getPhone());
            snapshot.put("province", address.getProvince());
            snapshot.put("city", address.getCity());
            snapshot.put("district", address.getDistrict());
            snapshot.put("detailAddress", address.getDetailAddress());
            return objectMapper.writeValueAsString(snapshot);
        } catch (Exception e) {
            log.error("地址序列化失败", e);
            return "{}";
        }
    }

    /**
     * 将列表序列化为 JSON 字符串
     */
    private String toJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            log.error("JSON 序列化失败", e);
            return null;
        }
    }
}
