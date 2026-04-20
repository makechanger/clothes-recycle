package com.recycle.controller.admin;

import cn.dev33.satoken.stp.StpLogic;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recycle.common.BusinessException;
import com.recycle.common.Result;
import com.recycle.dto.AdminLoginRequest;
import com.recycle.dto.CreateCollectorRequest;
import com.recycle.entity.Admin;
import com.recycle.mapper.AdminMapper;
import com.recycle.service.AdminCollectorService;
import com.recycle.service.AdminOrderService;
import com.recycle.service.AdminUserService;
import com.recycle.service.ComplaintService;
import com.recycle.service.PointsRuleService;
import com.recycle.service.ReviewService;
import com.recycle.service.RoleApplicationService;
import com.recycle.service.AdminStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员控制器
 * 处理管理员登录、资质申请审批等后台管理操作
 * 路由前缀 /api/admin，由 SaTokenConfig 中 StpLogic("admin") 鉴权保护
 * 其中 /api/admin/login 已放行
 */
@Tag(name = "管理员接口")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminMapper adminMapper;
    private final RoleApplicationService roleApplicationService;
    private final AdminCollectorService adminCollectorService;
    private final AdminOrderService adminOrderService;
    private final PointsRuleService pointsRuleService;
    private final AdminUserService adminUserService;
    private final ReviewService reviewService;
    private final ComplaintService complaintService;
    private final AdminStatisticsService adminStatisticsService;

    /**
     * 管理员登录
     * 使用用户名+密码登录，返回 token
     */
    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public Result<?> login(@RequestBody AdminLoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        if (username == null || password == null) {
            throw new BusinessException(400, "用户名和密码不能为空");
        }

        // 查询管理员
        Admin admin = adminMapper.selectOne(
                new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username)
        );
        if (admin == null) {
            throw new BusinessException(400, "管理员账号不存在");
        }
        if (!password.equals(admin.getPasswordHash())) {
            throw new BusinessException(400, "密码错误");
        }
        if (admin.getStatus() != 1) {
            throw new BusinessException(403, "账号已禁用");
        }

        // 使用 admin 鉴权体系登录
        StpLogic adminStpLogic = new StpLogic("admin");
        adminStpLogic.login(admin.getId());

        // 返回 token 和管理员信息
        Map<String, Object> data = new HashMap<>();
        data.put("token", adminStpLogic.getTokenValue());
        data.put("username", admin.getUsername());
        data.put("role", admin.getRole());
        return Result.success(data);
    }

    /**
     * 查看待审核的资质申请列表
     */
    @Operation(summary = "查看待审核申请列表")
    @GetMapping("/applications/pending")
    public Result<?> pendingApplications() {
        return Result.success(roleApplicationService.getPendingApplications());
    }

    /**
     * 查看已处理的审批记录（已通过+已拒绝）
     */
    @Operation(summary = "审批记录列表")
    @GetMapping("/applications/processed")
    public Result<?> processedApplications() {
        return Result.success(roleApplicationService.getProcessedApplications());
    }

    /**
     * 审批通过
     * 将用户从 user 表迁移到 collector/institution 表，保留原 id
     */
    @Operation(summary = "审批通过")
    @PostMapping("/applications/{id}/approve")
    public Result<?> approve(@PathVariable Long id) {
        roleApplicationService.approve(id);
        return Result.success(null);
    }

    /**
     * 审批拒绝
     */
    @Operation(summary = "审批拒绝")
    @PostMapping("/applications/{id}/reject")
    public Result<?> reject(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String reason = params.get("reason");
        roleApplicationService.reject(id, reason);
        return Result.success(null);
    }

    /**
     * 查询所有回收员列表
     * 关联 user 表和 collector 表，返回合并后的回收员信息
     */
    @Operation(summary = "回收员列表")
    @GetMapping("/collectors")
    public Result<?> listCollectors() {
        return Result.success(adminCollectorService.listCollectors());
    }

    /**
     * 管理员创建回收员账号
     * 在 user 表和 collector 表同时创建记录，初始状态为待完善资质
     */
    @Operation(summary = "创建回收员")
    @PostMapping("/collectors/create")
    public Result<?> createCollector(@RequestBody CreateCollectorRequest request) {
        adminCollectorService.createCollector(request);
        return Result.success(null);
    }

    // ==================== 订单管理 ====================

    /**
     * 分页查询所有订单
     * 支持按状态筛选、按订单号模糊搜索
     */
    @Operation(summary = "订单列表（分页）")
    @GetMapping("/orders")
    public Result<?> listOrders(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String orderNo,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(adminOrderService.listOrders(status, orderNo, page, size));
    }

    /**
     * 查询订单详情（含状态流转日志）
     */
    @Operation(summary = "订单详情")
    @GetMapping("/orders/{id}")
    public Result<?> getOrderDetail(@PathVariable Long id) {
        return Result.success(adminOrderService.getOrderDetail(id));
    }

    // ==================== 积分规则管理 ====================

    /**
     * 查询所有积分规则
     */
    @Operation(summary = "积分规则列表")
    @GetMapping("/points-rules")
    public Result<?> listPointsRules() {
        return Result.success(pointsRuleService.listRules());
    }

    /**
     * 更新积分规则
     * 可修改每公斤积分数、最低重量、启用/禁用状态
     */
    @Operation(summary = "更新积分规则")
    @PutMapping("/points-rules/{id}")
    public Result<?> updatePointsRule(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Integer pointsPerKg = params.get("pointsPerKg") != null ? ((Number) params.get("pointsPerKg")).intValue() : null;
        java.math.BigDecimal minWeight = params.get("minWeight") != null ? new java.math.BigDecimal(params.get("minWeight").toString()) : null;
        Integer ruleStatus = params.get("status") != null ? ((Number) params.get("status")).intValue() : null;
        pointsRuleService.updateRule(id, pointsPerKg, minWeight, ruleStatus);
        return Result.success(null);
    }

    // ==================== 用户管理 ====================

    /**
     * 分页查询用户列表
     * 支持按角色、状态筛选，按手机号/姓名模糊搜索
     */
    @Operation(summary = "用户列表（分页）")
    @GetMapping("/users")
    public Result<?> listUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(adminUserService.listUsers(role, status, keyword, page, size));
    }

    /**
     * 启用/禁用用户
     */
    @Operation(summary = "启用/禁用用户")
    @PutMapping("/users/{id}/status")
    public Result<?> toggleUserStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Integer status = params.get("status") != null ? ((Number) params.get("status")).intValue() : null;
        if (status == null) {
            throw new BusinessException(400, "status 参数不能为空");
        }
        adminUserService.toggleUserStatus(id, status);
        return Result.success(null);
    }

    /**
     * 重置用户密码为默认值
     */
    @Operation(summary = "重置用户密码")
    @PostMapping("/users/{id}/reset-password")
    public Result<?> resetUserPassword(@PathVariable Long id) {
        adminUserService.resetPassword(id);
        return Result.success(null);
    }

    /**
     * 修改用户角色
     */
    @Operation(summary = "修改用户角色")
    @PutMapping("/users/{id}/role")
    public Result<?> changeUserRole(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String role = params.get("role");
        if (role == null || role.isEmpty()) {
            throw new BusinessException(400, "role 参数不能为空");
        }
        adminUserService.changeRole(id, role);
        return Result.success(null);
    }

    /**
     * 删除用户（软删除，设为已注销状态）
     */
    @Operation(summary = "删除用户")
    @DeleteMapping("/users/{id}")
    public Result<?> deleteUser(@PathVariable Long id) {
        adminUserService.deleteUser(id);
        return Result.success(null);
    }

    // ==================== 评价管理 ====================

    /**
     * 分页查询评价列表
     */
    @Operation(summary = "评价列表（分页）")
    @GetMapping("/reviews")
    public Result<?> listReviews(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(reviewService.listReviews(page, size));
    }

    // ==================== 申诉管理 ====================

    /**
     * 分页查询申诉列表（可按状态筛选）
     */
    @Operation(summary = "申诉列表（分页）")
    @GetMapping("/complaints")
    public Result<?> listComplaints(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(complaintService.listComplaints(status, page, size));
    }

    /**
     * 处理申诉
     */
    @Operation(summary = "处理申诉")
    @PostMapping("/complaints/{id}/handle")
    public Result<?> handleComplaint(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        String adminRemark = (String) params.get("adminRemark");
        String action = (String) params.get("action");
        Integer refundAmount = params.get("refundAmount") != null ? ((Number) params.get("refundAmount")).intValue() : null;
        complaintService.handleComplaint(id, adminRemark, action, refundAmount);
        return Result.success(null);
    }

    // ==================== 数据统计 ====================

    /**
     * 数据统计概览
     * 返回概览卡片、订单趋势、状态分布、品类分布、回收员排行
     */
    @Operation(summary = "数据统计概览")
    @GetMapping("/statistics")
    public Result<?> getStatistics() {
        return Result.success(adminStatisticsService.getStatistics());
    }
}
