package com.recycle.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recycle.common.BusinessException;
import com.recycle.entity.Institution;
import com.recycle.entity.RecycleOrder;
import com.recycle.entity.User;
import com.recycle.mapper.InstitutionMapper;
import com.recycle.mapper.RecycleOrderMapper;
import com.recycle.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 机构后台数据统计服务
 * 提供机构维度的订单统计、趋势、品类分布、去向分布等数据
 */
@Service
@RequiredArgsConstructor
public class InstitutionStatisticsService {

    private final RecycleOrderMapper orderMapper;
    private final InstitutionMapper institutionMapper;
    private final UserMapper userMapper;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM-dd");

    private static final Map<String, String> DESTINATION_TYPE_MAP = new LinkedHashMap<>();
    static {
        DESTINATION_TYPE_MAP.put("DONATION", "捐赠");
        DESTINATION_TYPE_MAP.put("RECYCLE", "再生利用");
        DESTINATION_TYPE_MAP.put("ENVIRONMENTAL", "环保处理");
    }

    /**
     * 获取机构统计数据
     */
    public Map<String, Object> getStatistics(Long userId) {
        Institution institution = getInstitution(userId);
        Long institutionId = userId;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("overview", buildOverview(institutionId));
        result.put("orderTrend", buildOrderTrend(institutionId));
        result.put("categoryDistribution", buildCategoryDistribution(institutionId));
        result.put("destinationDistribution", buildDestinationDistribution(institutionId));
        return result;
    }

    private Institution getInstitution(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || !"INSTITUTION".equals(user.getRole())) {
            throw new BusinessException(403, "无权限，仅机构可操作");
        }
        Institution institution = institutionMapper.selectOne(
                new LambdaQueryWrapper<Institution>().eq(Institution::getUserId, userId));
        if (institution == null) {
            throw new BusinessException(404, "机构信息不存在");
        }
        return institution;
    }

    /** 概览卡片 */
    private Map<String, Object> buildOverview(Long institutionId) {
        Map<String, Object> overview = new LinkedHashMap<>();

        List<RecycleOrder> orders = orderMapper.selectList(
                new LambdaQueryWrapper<RecycleOrder>()
                        .eq(RecycleOrder::getInstitutionId, institutionId)
                        .in(RecycleOrder::getStatus, 7, 8));

        overview.put("totalOrders", orders.size());

        BigDecimal totalWeight = BigDecimal.ZERO;
        int totalPoints = 0;
        int assignedCount = 0;
        for (RecycleOrder order : orders) {
            if (order.getActualWeight() != null) {
                totalWeight = totalWeight.add(order.getActualWeight());
            }
            if (order.getPointsAwarded() != null) {
                totalPoints += order.getPointsAwarded();
            }
            if (order.getStatus() == 8) {
                assignedCount++;
            }
        }
        overview.put("totalWeight", totalWeight);
        overview.put("totalPoints", totalPoints);
        overview.put("assignedCount", assignedCount);
        overview.put("unassignedCount", orders.size() - assignedCount);

        return overview;
    }

    /** 近7天接收趋势 */
    private List<Map<String, Object>> buildOrderTrend(Long institutionId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startDate = today.minusDays(6).atStartOfDay();

        List<RecycleOrder> orders = orderMapper.selectList(
                new LambdaQueryWrapper<RecycleOrder>()
                        .eq(RecycleOrder::getInstitutionId, institutionId)
                        .in(RecycleOrder::getStatus, 7, 8)
                        .ge(RecycleOrder::getUpdatedAt, startDate));

        Map<String, Integer> dateCountMap = new HashMap<>();
        for (RecycleOrder order : orders) {
            if (order.getUpdatedAt() != null) {
                String key = order.getUpdatedAt().toLocalDate().format(DATE_FMT);
                dateCountMap.merge(key, 1, Integer::sum);
            }
        }

        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            String key = d.format(DATE_FMT);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", key);
            item.put("count", dateCountMap.getOrDefault(key, 0));
            trend.add(item);
        }
        return trend;
    }

    /** 品类分布 */
    private List<Map<String, Object>> buildCategoryDistribution(Long institutionId) {
        List<RecycleOrder> orders = orderMapper.selectList(
                new LambdaQueryWrapper<RecycleOrder>()
                        .eq(RecycleOrder::getInstitutionId, institutionId)
                        .in(RecycleOrder::getStatus, 7, 8)
                        .isNotNull(RecycleOrder::getClothesCategories)
                        .select(RecycleOrder::getClothesCategories));

        Map<String, Integer> categoryCount = new LinkedHashMap<>();
        for (RecycleOrder order : orders) {
            String json = order.getClothesCategories();
            if (json == null || json.isEmpty()) continue;
            try {
                List<String> categories = JSONUtil.parseArray(json).toList(String.class);
                for (String cat : categories) {
                    categoryCount.merge(cat, 1, Integer::sum);
                }
            } catch (Exception ignored) {
            }
        }

        List<Map<String, Object>> distribution = new ArrayList<>();
        categoryCount.forEach((name, count) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", name);
            item.put("value", count);
            distribution.add(item);
        });
        return distribution;
    }

    /** 衣物去向分布 */
    private List<Map<String, Object>> buildDestinationDistribution(Long institutionId) {
        List<RecycleOrder> orders = orderMapper.selectList(
                new LambdaQueryWrapper<RecycleOrder>()
                        .eq(RecycleOrder::getInstitutionId, institutionId)
                        .eq(RecycleOrder::getStatus, 8)
                        .isNotNull(RecycleOrder::getDestinationType)
                        .select(RecycleOrder::getDestinationType));

        Map<String, Integer> typeCount = new LinkedHashMap<>();
        for (RecycleOrder order : orders) {
            String type = order.getDestinationType();
            String label = DESTINATION_TYPE_MAP.getOrDefault(type, type);
            typeCount.merge(label, 1, Integer::sum);
        }

        List<Map<String, Object>> distribution = new ArrayList<>();
        typeCount.forEach((name, count) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", name);
            item.put("value", count);
            distribution.add(item);
        });
        return distribution;
    }
}
