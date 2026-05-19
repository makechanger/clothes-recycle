package com.recycle.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.recycle.common.BusinessException;
import com.recycle.common.Result;
import com.recycle.dto.CreateDestinationRequest;
import com.recycle.dto.LoginRequest;
import com.recycle.dto.ScanReceiveRequest;
import com.recycle.entity.Institution;
import com.recycle.entity.RecycleOrder;
import com.recycle.entity.User;
import com.recycle.mapper.InstitutionMapper;
import com.recycle.mapper.UserMapper;
import com.recycle.service.ClothingDestinationService;
import com.recycle.service.InstitutionOrderService;
import com.recycle.service.InstitutionStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 机构端控制器
 * 处理机构登录、数据统计、订单管理、扫码接收、衣物去向分配、机构信息维护
 */
@Slf4j
@Tag(name = "机构端接口")
@RestController
@RequestMapping("/api/institution")
@RequiredArgsConstructor
public class InstitutionController {

    private final InstitutionOrderService institutionOrderService;
    private final ClothingDestinationService clothingDestinationService;
    private final InstitutionStatisticsService institutionStatisticsService;
    private final UserMapper userMapper;
    private final InstitutionMapper institutionMapper;

    // ==================== 机构登录（公开接口，SaTokenConfig 中放行） ====================

    @Operation(summary = "机构登录")
    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginRequest request) {
        String phone = request.getPhone();
        String password = request.getPassword();
        if (phone == null || password == null) {
            throw new BusinessException(400, "手机号和密码不能为空");
        }

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (user == null) {
            throw new BusinessException(400, "账号不存在");
        }
        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            throw new BusinessException(400, "密码错误");
        }
        if (!"INSTITUTION".equals(user.getRole())) {
            throw new BusinessException(403, "该账号不是机构账号");
        }
        if (user.getStatus() != 1) {
            throw new BusinessException(403, "账号已禁用");
        }

        Institution institution = institutionMapper.selectOne(
                new LambdaQueryWrapper<Institution>().eq(Institution::getUserId, user.getId()));
        if (institution == null) {
            throw new BusinessException(404, "机构信息不存在");
        }
        if (institution.getStatus() != 1) {
            throw new BusinessException(403, "机构已被禁用");
        }

        StpUtil.login(user.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("token", StpUtil.getTokenValue());
        data.put("role", user.getRole());
        data.put("userInfo", Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "phone", user.getPhone()
        ));
        data.put("institution", Map.of(
                "id", institution.getId(),
                "name", institution.getName(),
                "address", institution.getAddress() != null ? institution.getAddress() : "",
                "contactPerson", institution.getContactPerson() != null ? institution.getContactPerson() : "",
                "type", institution.getType() != null ? institution.getType() : ""
        ));
        return Result.success(data);
    }

    // ==================== 数据统计 ====================

    @Operation(summary = "机构数据统计")
    @GetMapping("/statistics")
    public Result<?> getStatistics() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(institutionStatisticsService.getStatistics(userId));
    }

    // ==================== 机构信息 ====================

    @Operation(summary = "获取机构信息")
    @GetMapping("/info")
    public Result<?> getInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        Institution institution = institutionMapper.selectOne(
                new LambdaQueryWrapper<Institution>().eq(Institution::getUserId, userId));
        if (institution == null) {
            throw new BusinessException(404, "机构信息不存在");
        }
        return Result.success(institution);
    }

    @Operation(summary = "编辑机构信息")
    @PutMapping("/info")
    public Result<Void> updateInfo(@RequestBody Map<String, String> params) {
        Long userId = StpUtil.getLoginIdAsLong();
        Institution institution = institutionMapper.selectOne(
                new LambdaQueryWrapper<Institution>().eq(Institution::getUserId, userId));
        if (institution == null) {
            throw new BusinessException(404, "机构信息不存在");
        }

        LambdaUpdateWrapper<Institution> wrapper = new LambdaUpdateWrapper<Institution>()
                .eq(Institution::getId, institution.getId());

        if (params.containsKey("name")) {
            wrapper.set(Institution::getName, params.get("name"));
        }
        if (params.containsKey("address")) {
            wrapper.set(Institution::getAddress, params.get("address"));
        }
        if (params.containsKey("contactPerson")) {
            wrapper.set(Institution::getContactPerson, params.get("contactPerson"));
        }
        if (params.containsKey("type")) {
            wrapper.set(Institution::getType, params.get("type"));
        }
        wrapper.set(Institution::getUpdatedAt, LocalDateTime.now());

        institutionMapper.update(null, wrapper);
        return Result.success();
    }

    // ==================== 订单管理 ====================

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
