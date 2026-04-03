package com.recycle.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.recycle.common.Result;
import com.recycle.dto.ChangePasswordRequest;
import com.recycle.dto.RoleApplicationRequest;
import com.recycle.service.AuthService;
import com.recycle.service.RoleApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
}
