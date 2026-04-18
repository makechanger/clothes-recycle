package com.recycle.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recycle.common.BusinessException;
import com.recycle.dto.ComplaintRequest;
import com.recycle.entity.Complaint;
import com.recycle.entity.RecycleOrder;
import com.recycle.entity.User;
import com.recycle.mapper.ComplaintMapper;
import com.recycle.mapper.RecycleOrderMapper;
import com.recycle.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 申诉服务
 */
@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintMapper complaintMapper;
    private final RecycleOrderMapper recycleOrderMapper;
    private final PointsService pointsService;
    private final UserMapper userMapper;

    private static final Set<String> VALID_TYPES = Set.of(
            "WEIGHT_DISPUTE", "SERVICE_ISSUE", "ITEM_LOST", "OTHER"
    );

    /**
     * 提交申诉
     */
    public void submitComplaint(Long userId, ComplaintRequest req) {
        // 校验类型
        if (req.getType() == null || !VALID_TYPES.contains(req.getType())) {
            throw new BusinessException(400, "申诉类型无效");
        }
        if (req.getDescription() == null || req.getDescription().trim().isEmpty()) {
            throw new BusinessException(400, "申诉描述不能为空");
        }

        // 校验订单
        RecycleOrder order = recycleOrderMapper.selectById(req.getOrderId());
        if (order == null) {
            throw new BusinessException(400, "订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权对此订单申诉");
        }

        Complaint complaint = new Complaint();
        complaint.setOrderId(req.getOrderId());
        complaint.setUserId(userId);
        complaint.setType(req.getType());
        complaint.setDescription(req.getDescription().trim());
        complaint.setStatus(0);
        complaintMapper.insert(complaint);
    }

    /**
     * 查询用户的申诉列表
     */
    public List<Complaint> getMyComplaints(Long userId) {
        return complaintMapper.selectList(
                new LambdaQueryWrapper<Complaint>()
                        .eq(Complaint::getUserId, userId)
                        .orderByDesc(Complaint::getCreatedAt)
        );
    }

    /**
     * 管理员分页查询申诉列表（可按状态筛选）
     */
    public Map<String, Object> listComplaints(Integer status, Integer page, Integer size) {
        Page<Complaint> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Complaint> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Complaint::getStatus, status);
        }
        wrapper.orderByDesc(Complaint::getCreatedAt);
        Page<Complaint> result = complaintMapper.selectPage(pageParam, wrapper);

        List<Complaint> complaints = result.getRecords();

        Set<Long> orderIds = complaints.stream().map(Complaint::getOrderId).collect(Collectors.toSet());
        Set<Long> userIds = complaints.stream().map(Complaint::getUserId).collect(Collectors.toSet());

        Map<Long, String> orderNoMap = new HashMap<>();
        if (!orderIds.isEmpty()) {
            recycleOrderMapper.selectBatchIds(orderIds).forEach(o -> orderNoMap.put(o.getId(), o.getOrderNo()));
        }
        Map<Long, String> userNameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMapper.selectBatchIds(userIds).forEach(u -> userNameMap.put(u.getId(), u.getName()));
        }

        List<Map<String, Object>> enriched = complaints.stream().map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("orderId", c.getOrderId());
            m.put("orderNo", orderNoMap.getOrDefault(c.getOrderId(), ""));
            m.put("userId", c.getUserId());
            m.put("userName", userNameMap.getOrDefault(c.getUserId(), ""));
            m.put("type", c.getType());
            m.put("description", c.getDescription());
            m.put("status", c.getStatus());
            m.put("adminRemark", c.getAdminRemark());
            m.put("action", c.getAction());
            m.put("refundAmount", c.getRefundAmount());
            m.put("createdAt", c.getCreatedAt());
            m.put("handledAt", c.getHandledAt());
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("records", enriched);
        data.put("total", result.getTotal());
        data.put("page", page);
        data.put("size", size);
        return data;
    }

    private static final Set<String> VALID_ACTIONS = Set.of(
            "MARK_ABNORMAL", "ADD_POINTS", "DEDUCT_POINTS"
    );

    /**
     * 管理员处理申诉
     */
    @Transactional
    public void handleComplaint(Long complaintId, String adminRemark, String action, Integer refundAmount) {
        Complaint complaint = complaintMapper.selectById(complaintId);
        if (complaint == null) {
            throw new BusinessException(400, "申诉不存在");
        }
        if (complaint.getStatus() != 0) {
            throw new BusinessException(400, "该申诉已处理");
        }

        if (action == null || !VALID_ACTIONS.contains(action)) {
            throw new BusinessException(400, "请选择处理方式");
        }

        switch (action) {
            case "ADD_POINTS":
                if (refundAmount == null || refundAmount <= 0) {
                    throw new BusinessException(400, "增加积分数量必须大于0");
                }
                pointsService.addPoints(complaint.getUserId(), refundAmount, complaint.getOrderId(), "申诉增加积分");
                break;
            case "DEDUCT_POINTS":
                if (refundAmount == null || refundAmount <= 0) {
                    throw new BusinessException(400, "扣减积分数量必须大于0");
                }
                pointsService.adminDeductPoints(complaint.getUserId(), refundAmount, complaint.getOrderId(), "申诉扣减积分");
                break;
            case "MARK_ABNORMAL":
                RecycleOrder order = recycleOrderMapper.selectById(complaint.getOrderId());
                if (order == null) {
                    throw new BusinessException(400, "关联订单不存在");
                }
                order.setStatus(6);
                recycleOrderMapper.updateById(order);
                break;
            default:
                break;
        }

        complaint.setStatus(1);
        complaint.setAdminRemark(adminRemark != null ? adminRemark : "");
        complaint.setAction(action);
        complaint.setRefundAmount(refundAmount);
        complaint.setHandledAt(LocalDateTime.now());
        complaintMapper.updateById(complaint);
    }
}
