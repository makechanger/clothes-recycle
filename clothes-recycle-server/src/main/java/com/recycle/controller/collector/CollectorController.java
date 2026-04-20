package com.recycle.controller.collector;

import cn.dev33.satoken.stp.StpUtil;
import com.recycle.common.Result;
import com.recycle.dto.CompleteWeighingRequest;
import com.recycle.entity.RecycleOrder;
import com.recycle.service.CollectorOrderService;
import com.recycle.service.OrderStatusLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 回收员端控制器
 * 处理回收员特有的业务操作：查看待接单、接单、出发上门、完成称重
 * 路由前缀 /api/collector，由 SaTokenConfig 中 StpUtil 鉴权保护
 * 角色校验在 Service 层完成（检查 user.role == COLLECTOR）
 */
@Tag(name = "回收员端接口")
@RestController
@RequestMapping("/api/collector")
@RequiredArgsConstructor
public class CollectorController {

    private final CollectorOrderService collectorOrderService;
    private final OrderStatusLogService orderStatusLogService;

    /**
     * 查看待接单列表
     * 返回所有 status=0 的订单，回收员可从中选择接单
     */
    @Operation(summary = "查看待接单列表")
    @GetMapping("/order/pending")
    public Result<List<RecycleOrder>> pendingOrders() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(collectorOrderService.pendingList(userId));
    }

    /**
     * 查看我的订单列表
     * 返回当前回收员已接的订单，支持按状态筛选
     */
    @Operation(summary = "查看我的订单列表")
    @GetMapping("/order/list")
    public Result<List<RecycleOrder>> myOrders(@RequestParam(required = false) Integer status) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(collectorOrderService.myOrders(userId, status));
    }

    /**
     * 接单
     * 将待接单订单分配给当前回收员，使用乐观更新防止并发
     */
    @Operation(summary = "接单")
    @PostMapping("/order/{id}/accept")
    public Result<?> acceptOrder(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        collectorOrderService.acceptOrder(id, userId);
        return Result.success();
    }

    /**
     * 开始上门（出发）
     * 回收员确认出发，订单状态从已接单变为上门中
     */
    @Operation(summary = "开始上门")
    @PostMapping("/order/{id}/pickup")
    public Result<?> startPickup(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        collectorOrderService.startPickup(id, userId);
        return Result.success();
    }

    /**
     * 查看订单详情
     * 回收员查看自己接的某个订单的完整信息，包含归属校验
     */
    @Operation(summary = "查看订单详情（含状态时间线）")
    @GetMapping("/order/{id}")
    public Result<Map<String, Object>> orderDetail(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        RecycleOrder order = collectorOrderService.getOrderDetail(id, userId);
        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("statusLogs", orderStatusLogService.listByOrderId(id));
        return Result.success(result);
    }

    /**
     * 完成称重
     * 回收员填写实际重量，订单进入待确认状态等待用户确认
     */
    @Operation(summary = "完成称重")
    @PostMapping("/order/{id}/complete")
    public Result<?> completeWeighing(@PathVariable Long id, @RequestBody CompleteWeighingRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        collectorOrderService.completeWeighing(id, userId, request.getActualWeight());
        return Result.success();
    }
}
