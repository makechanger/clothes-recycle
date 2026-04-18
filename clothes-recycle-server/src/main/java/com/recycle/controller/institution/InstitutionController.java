package com.recycle.controller.institution;

import com.recycle.common.Result;
import com.recycle.dto.ScanReceiveRequest;
import com.recycle.entity.RecycleOrder;
import com.recycle.service.InstitutionOrderService;
import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 机构端控制器
 * 处理机构特有的业务操作：扫码接收订单、查看已接收订单列表
 * 路由前缀 /api/institution，由 SaTokenConfig 中 StpUtil 鉴权保护
 */
@Slf4j
@Tag(name = "机构端接口")
@RestController
@RequestMapping("/api/institution")
@RequiredArgsConstructor
public class InstitutionController {

    private final InstitutionOrderService institutionOrderService;

    /**
     * 机构扫码接收订单
     * 扫描回收员生成的溯源二维码，完成订单接收并触发积分发放
     *
     * @param request 扫码请求（包含订单编号）
     * @return 操作结果
     */
    @Operation(summary = "扫码接收订单")
    @PostMapping("/order/receive")
    public Result<Void> scanReceive(@RequestBody ScanReceiveRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        institutionOrderService.scanReceive(userId, request.getOrderNo());
        return Result.success();
    }

    /**
     * 查询机构已接收的订单列表
     * 支持按状态筛选，默认查全部已接收订单
     *
     * @param status 订单状态（可选，null 表示查全部）
     * @return 订单列表
     */
    @Operation(summary = "已接收订单列表")
    @GetMapping("/order/list")
    public Result<List<RecycleOrder>> listReceivedOrders(
            @RequestParam(required = false) Integer status) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<RecycleOrder> orders = institutionOrderService.listReceivedOrders(userId, status);
        return Result.success(orders);
    }
}
