package com.recycle.controller.institution;

import com.recycle.common.Result;
import com.recycle.dto.CreateDestinationRequest;
import com.recycle.dto.ScanReceiveRequest;
import com.recycle.entity.RecycleOrder;
import com.recycle.service.ClothingDestinationService;
import com.recycle.service.InstitutionOrderService;
import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 机构端控制器
 * 处理机构特有的业务操作：扫码接收订单、分配衣物去向
 */
@Slf4j
@Tag(name = "机构端接口")
@RestController
@RequestMapping("/api/institution")
@RequiredArgsConstructor
public class InstitutionController {

    private final InstitutionOrderService institutionOrderService;
    private final ClothingDestinationService clothingDestinationService;

    @Operation(summary = "扫码接收订单")
    @PostMapping("/order/receive")
    public Result<Void> scanReceive(@RequestBody ScanReceiveRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        institutionOrderService.scanReceive(userId, request.getOrderNo());
        return Result.success();
    }

    @Operation(summary = "订单详情")
    @GetMapping("/order/{id}")
    public Result<Map<String, Object>> orderDetail(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(institutionOrderService.getOrderDetail(userId, id));
    }

    @Operation(summary = "已接收订单列表")
    @GetMapping("/order/list")
    public Result<List<RecycleOrder>> listReceivedOrders(
            @RequestParam(required = false) Integer status) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<RecycleOrder> orders = institutionOrderService.listReceivedOrders(userId, status);
        return Result.success(orders);
    }

    // ==================== 衣物去向管理 ====================

    @Operation(summary = "为订单分配去向")
    @PostMapping("/destination/assign")
    public Result<Void> assignDestination(@RequestBody CreateDestinationRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        clothingDestinationService.assignDestination(
                userId, request.getOrderId(), request.getDestinationType(), request.getDescription());
        return Result.success();
    }

    @Operation(summary = "查询未分配去向的已接收订单")
    @GetMapping("/destination/unassigned-orders")
    public Result<List<RecycleOrder>> listUnassignedOrders() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(clothingDestinationService.listUnassignedOrders(userId));
    }
}
