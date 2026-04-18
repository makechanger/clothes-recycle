package com.recycle.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recycle.common.BusinessException;
import com.recycle.dto.ReviewRequest;
import com.recycle.entity.RecycleOrder;
import com.recycle.entity.ServiceReview;
import com.recycle.entity.User;
import com.recycle.mapper.RecycleOrderMapper;
import com.recycle.mapper.ServiceReviewMapper;
import com.recycle.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 评价服务
 */
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ServiceReviewMapper serviceReviewMapper;
    private final RecycleOrderMapper recycleOrderMapper;
    private final PointsService pointsService;
    private final UserMapper userMapper;

    /**
     * 提交评价
     * 校验订单归属、状态、是否已评价，插入评价记录并发放 +2 积分奖励
     */
    @Transactional
    public ServiceReview submitReview(Long userId, ReviewRequest req) {
        // 校验评分范围
        validateScore(req.getPunctualityScore(), "准时度");
        validateScore(req.getAttitudeScore(), "态度");
        validateScore(req.getWeighingScore(), "称重规范度");

        // 校验订单
        RecycleOrder order = recycleOrderMapper.selectById(req.getOrderId());
        if (order == null) {
            throw new BusinessException(400, "订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权评价此订单");
        }
        if (order.getStatus() != 7) {
            throw new BusinessException(400, "仅机构已接收的订单可以评价");
        }

        // 检查是否已评价
        Long count = serviceReviewMapper.selectCount(
                new LambdaQueryWrapper<ServiceReview>().eq(ServiceReview::getOrderId, req.getOrderId())
        );
        if (count > 0) {
            throw new BusinessException(400, "该订单已评价");
        }

        // 插入评价记录
        ServiceReview review = new ServiceReview();
        review.setOrderId(req.getOrderId());
        review.setUserId(userId);
        review.setCollectorId(order.getCollectorId());
        review.setPunctualityScore(req.getPunctualityScore());
        review.setAttitudeScore(req.getAttitudeScore());
        review.setWeighingScore(req.getWeighingScore());
        review.setContent(req.getContent());
        serviceReviewMapper.insert(review);

        // 发放 +2 积分奖励
        pointsService.addPoints(userId, 2, order.getId(), "评价奖励");

        return review;
    }

    /**
     * 查询订单的评价记录（用于前端判断是否已评价）
     */
    public ServiceReview getReviewByOrderId(Long orderId) {
        return serviceReviewMapper.selectOne(
                new LambdaQueryWrapper<ServiceReview>().eq(ServiceReview::getOrderId, orderId)
        );
    }

    /**
     * 管理员分页查询评价列表
     */
    public Map<String, Object> listReviews(Integer page, Integer size) {
        Page<ServiceReview> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ServiceReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ServiceReview::getCreatedAt);
        Page<ServiceReview> result = serviceReviewMapper.selectPage(pageParam, wrapper);

        List<ServiceReview> reviews = result.getRecords();

        Set<Long> orderIds = reviews.stream().map(ServiceReview::getOrderId).collect(Collectors.toSet());
        Set<Long> userIds = reviews.stream().map(ServiceReview::getUserId).collect(Collectors.toSet());
        Set<Long> collectorIds = reviews.stream().map(ServiceReview::getCollectorId).collect(Collectors.toSet());

        Map<Long, String> orderNoMap = new HashMap<>();
        if (!orderIds.isEmpty()) {
            recycleOrderMapper.selectBatchIds(orderIds).forEach(o -> orderNoMap.put(o.getId(), o.getOrderNo()));
        }
        Map<Long, String> userNameMap = new HashMap<>();
        Set<Long> allUserIds = new java.util.HashSet<>(userIds);
        allUserIds.addAll(collectorIds);
        if (!allUserIds.isEmpty()) {
            userMapper.selectBatchIds(allUserIds).forEach(u -> userNameMap.put(u.getId(), u.getName()));
        }

        List<Map<String, Object>> enriched = reviews.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("orderId", r.getOrderId());
            m.put("orderNo", orderNoMap.getOrDefault(r.getOrderId(), ""));
            m.put("userId", r.getUserId());
            m.put("userName", userNameMap.getOrDefault(r.getUserId(), ""));
            m.put("collectorId", r.getCollectorId());
            m.put("collectorName", userNameMap.getOrDefault(r.getCollectorId(), ""));
            m.put("punctualityScore", r.getPunctualityScore());
            m.put("attitudeScore", r.getAttitudeScore());
            m.put("weighingScore", r.getWeighingScore());
            m.put("content", r.getContent());
            m.put("createdAt", r.getCreatedAt());
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("records", enriched);
        data.put("total", result.getTotal());
        data.put("page", page);
        data.put("size", size);
        return data;
    }

    private void validateScore(Integer score, String name) {
        if (score == null || score < 1 || score > 5) {
            throw new BusinessException(400, name + "评分必须在1-5之间");
        }
    }
}
