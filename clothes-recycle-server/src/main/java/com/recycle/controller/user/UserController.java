package com.recycle.controller.user;

import cn.dev33.satoken.stp.StpUtil;
import com.recycle.common.Result;
import com.recycle.dto.ChangePasswordRequest;
import com.recycle.dto.ComplaintRequest;
import com.recycle.dto.CreateOrderRequest;
import com.recycle.dto.ReviewRequest;
import com.recycle.dto.RoleApplicationRequest;
import com.recycle.dto.UserAddressRequest;
import com.recycle.entity.RecycleOrder;
import com.recycle.entity.ServiceReview;
import com.recycle.entity.User;
import com.recycle.entity.UserAddress;
import com.recycle.service.AuthService;
import com.recycle.service.ClothingDestinationService;
import com.recycle.service.ComplaintService;
import com.recycle.service.OrderStatusLogService;
import com.recycle.service.PointsService;
import com.recycle.service.RecycleOrderService;
import com.recycle.service.ReviewService;
import com.recycle.service.RoleApplicationService;
import com.recycle.service.UserAddressService;
import com.recycle.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户端控制器
 * 处理所有角色的通用操作（修改密码、资质申请等）
 * 路由前缀 /api/user，由 SaTokenConfig 中 StpUtil 鉴权保护
 * 重构后所有角色统一使用 StpUtil，修改密码等操作统一在此处理
 */
@Tag(name = "用户端接口")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final RoleApplicationService roleApplicationService;
    private final UserAddressService userAddressService;
    private final RecycleOrderService recycleOrderService;
    private final ReviewService reviewService;
    private final ComplaintService complaintService;
    private final ClothingDestinationService clothingDestinationService;
    private final PointsService pointsService;
    private final OrderStatusLogService orderStatusLogService;
    private final UserMapper userMapper;

    /**
     * 修改密码（所有角色通用）
     * 重构后所有角色的密码都在 user 表中，统一通过此接口修改
     */
    @Operation(summary = "修改密码（所有角色通用）")
    @PostMapping("/changePassword")
    public Result<?> changePassword(@RequestBody ChangePasswordRequest request) {
        // 获取当前登录用户 ID（所有角色统一使用 StpUtil）
        Long userId = StpUtil.getLoginIdAsLong();
        authService.changePassword(userId, request);
        return Result.success(null);
    }

    /**
     * 提交资质申请
     * 用户申请成为回收员或机构，提交后等待管理员审批
     */
    @Operation(summary = "提交资质申请")
    @PostMapping("/applyRole")
    public Result<?> applyRole(@RequestBody RoleApplicationRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        roleApplicationService.apply(userId, request);
        return Result.success(null);
    }

    /**
     * 查看我的申请记录
     * 返回当前用户的所有资质申请记录及审核状态
     */
    @Operation(summary = "查看我的申请记录")
    @GetMapping("/myApplications")
    public Result<?> myApplications() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(roleApplicationService.getMyApplications(userId));
    }

    // ==================== 地址管理接口 ====================

    /**
     * 获取当前用户的地址列表
     * 默认地址排在最前面，其余按创建时间倒序
     */
    @Operation(summary = "获取我的地址列表")
    @GetMapping("/address/list")
    public Result<List<UserAddress>> addressList() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(userAddressService.listByUserId(userId));
    }

    /**
     * 获取地址详情
     * 包含归属校验，只能查看自己的地址
     */
    @Operation(summary = "获取地址详情")
    @GetMapping("/address/{id}")
    public Result<UserAddress> addressDetail(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(userAddressService.getById(id, userId));
    }

    /**
     * 新增地址
     * 如果 isDefault=1，会自动取消其他默认地址
     */
    @Operation(summary = "新增地址")
    @PostMapping("/address/create")
    public Result<?> addressCreate(@RequestBody UserAddressRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        userAddressService.create(userId, request);
        return Result.success();
    }

    /**
     * 编辑地址
     * 包含归属校验，只能编辑自己的地址
     */
    @Operation(summary = "编辑地址")
    @PostMapping("/address/{id}/update")
    public Result<?> addressUpdate(@PathVariable Long id, @RequestBody UserAddressRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        userAddressService.update(id, userId, request);
        return Result.success();
    }

    /**
     * 删除地址
     * 包含归属校验，只能删除自己的地址
     */
    @Operation(summary = "删除地址")
    @PostMapping("/address/{id}/delete")
    public Result<?> addressDelete(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        userAddressService.delete(id, userId);
        return Result.success();
    }

    /**
     * 设置默认地址
     * 会先取消该用户所有默认地址，再将指定地址设为默认
     */
    @Operation(summary = "设置默认地址")
    @PostMapping("/address/{id}/setDefault")
    public Result<?> addressSetDefault(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        userAddressService.setDefault(id, userId);
        return Result.success();
    }

    // ==================== 回收订单接口 ====================

    /**
     * 创建回收订单
     * 用户填写预约信息后提交，生成待接单状态的订单
     */
    @Operation(summary = "创建回收订单")
    @PostMapping("/order/create")
    public Result<RecycleOrder> orderCreate(@RequestBody CreateOrderRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        RecycleOrder order = recycleOrderService.createOrder(userId, request);
        return Result.success(order);
    }

    /**
     * 查询我的订单列表
     * 支持按状态筛选，不传 status 则查全部，按创建时间倒序
     */
    @Operation(summary = "查询我的订单列表")
    @GetMapping("/order/list")
    public Result<List<RecycleOrder>> orderList(@RequestParam(required = false) Integer status) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(recycleOrderService.listByUser(userId, status));
    }

    /**
     * 查询订单详情
     * 包含归属校验，只能查看自己的订单
     */
    @Operation(summary = "查询订单详情（含状态时间线和回收员信息）")
    @GetMapping("/order/{id}")
    public Result<Map<String, Object>> orderDetail(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        RecycleOrder order = recycleOrderService.getDetail(id, userId);
        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("statusLogs", orderStatusLogService.listByOrderId(id));
        // 已接单时补充回收员姓名和手机号
        if (order.getCollectorId() != null) {
            User collector = userMapper.selectById(order.getCollectorId());
            if (collector != null) {
                Map<String, Object> collectorInfo = new HashMap<>();
                collectorInfo.put("name", collector.getName());
                collectorInfo.put("phone", collector.getPhone());
                result.put("collectorInfo", collectorInfo);
            }
        }
        // 机构已接收或已分配去向时补充衣物去向信息
        if (order.getStatus() != null && order.getStatus() >= 7) {
            result.put("destination", clothingDestinationService.getOrderDestination(id));
        }
        return Result.success(result);
    }

    /**
     * 取消订单
     * 仅待接单(0)和已接单(1)状态可取消
     */
    @Operation(summary = "取消订单")
    @PostMapping("/order/{id}/cancel")
    public Result<?> orderCancel(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        recycleOrderService.cancelOrder(id, userId);
        return Result.success();
    }

    /**
     * 确认完成订单
     * 仅待确认(3)状态可确认，确认后按实际重量发放积分
     */
    @Operation(summary = "确认完成订单")
    @PostMapping("/order/{id}/confirm")
    public Result<?> orderConfirm(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        recycleOrderService.confirmOrder(id, userId);
        return Result.success();
    }

    // ==================== 评价接口 ====================

    /**
     * 提交评价
     * 仅机构已接收(status=7)的订单可评价，每个订单只能评价一次，评价后 +2 积分
     */
    @Operation(summary = "提交评价")
    @PostMapping("/review/submit")
    public Result<ServiceReview> reviewSubmit(@RequestBody ReviewRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        ServiceReview review = reviewService.submitReview(userId, request);
        return Result.success(review);
    }

    /**
     * 查询订单评价（判断是否已评价）
     */
    @Operation(summary = "查询订单评价")
    @GetMapping("/review/{orderId}")
    public Result<ServiceReview> getReview(@PathVariable Long orderId) {
        return Result.success(reviewService.getReviewByOrderId(orderId));
    }

    // ==================== 申诉接口 ====================

    /**
     * 提交申诉
     */
    @Operation(summary = "提交申诉")
    @PostMapping("/complaint/submit")
    public Result<?> complaintSubmit(@RequestBody ComplaintRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        complaintService.submitComplaint(userId, request);
        return Result.success();
    }

    /**
     * 查询我的申诉列表
     */
    @Operation(summary = "我的申诉列表")
    @GetMapping("/complaint/list")
    public Result<?> complaintList() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(complaintService.getMyComplaints(userId));
    }

    // ==================== 积分接口 ====================

    @Operation(summary = "积分流水明细")
    @GetMapping("/points/history")
    public Result<?> pointsHistory() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(pointsService.getPointsHistory(userId));
    }
}
