package com.recycle.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recycle.entity.RecycleOrder;
import com.recycle.entity.User;
import com.recycle.mapper.RecycleOrderMapper;
import com.recycle.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 管理后台数据统计服务
 * 聚合订单、用户、回收员等维度的统计数据，供仪表盘展示
 */
@Service
@RequiredArgsConstructor
public class AdminStatisticsService {

    private final UserMapper userMapper;
    private final RecycleOrderMapper orderMapper;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM-dd");

    /** 状态码→中文映射 */
    private static final Map<Integer, String> STATUS_NAME_MAP = new LinkedHashMap<>();
    static {
        STATUS_NAME_MAP.put(0, "待接单");
        STATUS_NAME_MAP.put(1, "已接单");
        STATUS_NAME_MAP.put(2, "上门中");
        STATUS_NAME_MAP.put(3, "待确认");
        STATUS_NAME_MAP.put(4, "已完成");
        STATUS_NAME_MAP.put(5, "已取消");
        STATUS_NAME_MAP.put(6, "异常");
        STATUS_NAME_MAP.put(7, "机构已接收");
    }

    /**
     * 获取统计数据总览
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("overview", buildOverview());
        result.put("orderTrend", buildOrderTrend());
        result.put("orderStatusDistribution", buildStatusDistribution());
        result.put("categoryDistribution", buildCategoryDistribution());
        result.put("collectorRanking", buildCollectorRanking());
        return result;
    }

    /** 概览卡片数据 */
    private Map<String, Object> buildOverview() {
        Map<String, Object> overview = new LinkedHashMap<>();

        // 按角色统计用户数
        overview.put("userCount", userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getRole, "USER")));
        overview.put("collectorCount", userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getRole, "COLLECTOR")));
        overview.put("institutionCount", userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getRole, "INSTITUTION")));

        // 订单总数 & 已完成订单数
        overview.put("orderCount", orderMapper.selectCount(null));
        overview.put("completedOrderCount", orderMapper.selectCount(
                new LambdaQueryWrapper<RecycleOrder>().in(RecycleOrder::getStatus, 4, 7)));

        // 积分与重量汇总
        Map<String, Object> stats = orderMapper.sumOrderStats();
        overview.put("totalPointsAwarded", stats.get("totalPoints"));
        overview.put("totalWeight", stats.get("totalWeight"));

        return overview;
    }

    /** 近7天订单趋势（含补零） */
    private List<Map<String, Object>> buildOrderTrend() {
        LocalDate today = LocalDate.now();
        LocalDateTime startDate = today.minusDays(6).atStartOfDay();
        List<Map<String, Object>> dbRows = orderMapper.countOrdersByDate(startDate);

        // 将数据库结果转为 date→count 的映射
        Map<String, Object> dateCountMap = new HashMap<>();
        for (Map<String, Object> row : dbRows) {
            String dateStr = row.get("date").toString();
            // MySQL DATE() 返回 yyyy-MM-dd 格式
            LocalDate d = LocalDate.parse(dateStr);
            dateCountMap.put(d.format(DATE_FMT), row.get("count"));
        }

        // 遍历7天，无数据补零
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            String key = d.format(DATE_FMT);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", key);
            item.put("count", dateCountMap.getOrDefault(key, 0L));
            trend.add(item);
        }
        return trend;
    }

    /** 订单状态分布 */
    private List<Map<String, Object>> buildStatusDistribution() {
        List<Map<String, Object>> dbRows = orderMapper.countOrdersByStatus();
        List<Map<String, Object>> distribution = new ArrayList<>();
        for (Map<String, Object> row : dbRows) {
            Integer statusCode = ((Number) row.get("status")).intValue();
            String name = STATUS_NAME_MAP.getOrDefault(statusCode, "未知");
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", name);
            item.put("value", row.get("count"));
            distribution.add(item);
        }
        return distribution;
    }

    /** 衣物品类分布（解析 JSON 数组字段） */
    private List<Map<String, Object>> buildCategoryDistribution() {
        // 查询所有未取消订单的 clothesCategories 字段
        List<RecycleOrder> orders = orderMapper.selectList(
                new LambdaQueryWrapper<RecycleOrder>()
                        .ne(RecycleOrder::getStatus, 5)
                        .isNotNull(RecycleOrder::getClothesCategories)
                        .select(RecycleOrder::getClothesCategories));

        // 统计各品类出现次数
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

        // 转为 ECharts 格式
        List<Map<String, Object>> distribution = new ArrayList<>();
        categoryCount.forEach((name, count) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", name);
            item.put("value", count);
            distribution.add(item);
        });
        return distribution;
    }

    /** 回收员接单排行 Top5 */
    private List<Map<String, Object>> buildCollectorRanking() {
        List<Map<String, Object>> dbRows = orderMapper.topCollectors();
        if (dbRows.isEmpty()) return Collections.emptyList();

        // 批量查询回收员姓名
        List<Long> collectorIds = new ArrayList<>();
        for (Map<String, Object> row : dbRows) {
            collectorIds.add(((Number) row.get("collector_id")).longValue());
        }
        List<User> users = userMapper.selectBatchIds(collectorIds);
        Map<Long, String> idNameMap = new HashMap<>();
        for (User u : users) {
            idNameMap.put(u.getId(), u.getName());
        }

        // 组装结果
        List<Map<String, Object>> ranking = new ArrayList<>();
        for (Map<String, Object> row : dbRows) {
            Long cid = ((Number) row.get("collector_id")).longValue();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", idNameMap.getOrDefault(cid, "未知回收员"));
            item.put("count", row.get("count"));
            ranking.add(item);
        }
        return ranking;
    }
}
