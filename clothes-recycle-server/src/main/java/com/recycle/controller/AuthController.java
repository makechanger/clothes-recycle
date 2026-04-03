package com.recycle.controller;

import com.recycle.common.Result;
import com.recycle.dto.LoginRequest;
import com.recycle.dto.LoginResponse;
import com.recycle.dto.RegisterRequest;
import com.recycle.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统一登录控制器
 * 所有角色（用户/回收员/机构）共用一个登录入口
 * 后端根据手机号自动识别角色
 */
@Tag(name = "登录认证", description = "统一登录接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 统一登录
     * 手机号+密码登录，后端自动识别是用户、回收员还是机构
     */
    @Operation(summary = "统一登录", description = "手机号+密码，后端自动识别角色")
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success(response);
    }

    /**
     * 用户注册
     * 所有角色先注册为普通用户，后续在个人中心申请升级角色
     */
    @Operation(summary = "用户注册", description = "手机号+密码+姓名，注册后自动登录")
    @PostMapping("/register")
    public Result<LoginResponse> register(@RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(request);
        return Result.success(response);
    }
}
