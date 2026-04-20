package com.recycle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.recycle.entity.RecycleOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 回收订单 Mapper
 * 继承 MyBatis-Plus BaseMapper，提供单表 CRUD 操作
 */
@Mapper
public interface RecycleOrderMapper extends BaseMapper<RecycleOrder> {

    /** 按日期统计订单数量（近N天） */
    @Select("SELECT DATE(created_at) as date, COUNT(*) as count FROM recycle_order WHERE created_at >= #{startDate} GROUP BY DATE(created_at) ORDER BY date")
    List<Map<String, Object>> countOrdersByDate(@Param("startDate") LocalDateTime startDate);

    /** 按状态统计订单数量 */
    @Select("SELECT status, COUNT(*) as count FROM recycle_order GROUP BY status")
    List<Map<String, Object>> countOrdersByStatus();

    /** 汇总已完成订单的积分和重量 */
    @Select("SELECT COALESCE(SUM(points_awarded),0) as totalPoints, COALESCE(SUM(actual_weight),0) as totalWeight FROM recycle_order WHERE status IN (4,7)")
    Map<String, Object> sumOrderStats();

    /** 回收员接单排行 Top5 */
    @Select("SELECT collector_id, COUNT(*) as count FROM recycle_order WHERE collector_id IS NOT NULL GROUP BY collector_id ORDER BY count DESC LIMIT 5")
    List<Map<String, Object>> topCollectors();
}
