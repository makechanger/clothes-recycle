package com.recycle.controller;

import cn.dev33.satoken.stp.StpLogic;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recycle.common.BusinessException;
import com.recycle.common.Result;
import com.recycle.dto.AdminLoginRequest;
import com.recycle.entity.Admin;
import com.recycle.mapper.AdminMapper;
import com.recycle.service.RoleApplicationService;
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
}
