package com.recycle.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 鉴权拦截器配置
 *
 * - 所有普通用户/回收员/机构统一使用 StpUtil（默认鉴权体系）
 * - 管理员独立使用 StpLogic("admin") 鉴权体系
 * - /api/user/**、/api/collector/**、/api/institution/** 都用 StpUtil.checkLogin()
 * - /api/admin/** 用 StpLogic("admin").checkLogin()
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {

            // 1. 公共放行路由（不需要任何登录）
            SaRouter.match("/**")
                    .notMatch(
                            "/api/auth/login",          // 统一登录（手机号+密码）
                            "/api/auth/register",       // 用户注册
                            "/api/admin/login",         // 管理员登录
                            "/api/common/**",           // 公共接口（如文件上传）
                            "/api/test",                // 测试接口
                            "/doc.html",                // Knife4j 文档页面
                            "/webjars/**",              // Knife4j 静态资源
                            "/v3/api-docs/**",          // OpenAPI 数据接口
                            "/swagger-ui/**",           // Swagger UI
                            "/favicon.ico"
                    );

            // 2. 用户/回收员/机构统一使用 StpUtil 鉴权（所有角色都在 user 表中）
            SaRouter.match("/api/user/**")
                    .check(r -> StpUtil.checkLogin());

            SaRouter.match("/api/collector/**")
                    .check(r -> StpUtil.checkLogin());

            SaRouter.match("/api/institution/**")
                    .check(r -> StpUtil.checkLogin());

            // 3. 管理员独立鉴权体系（admin 表独立，不在 user 表中）
            SaRouter.match("/api/admin/**")
                    .notMatch("/api/admin/login")
                    .check(r -> new StpLogic("admin").checkLogin());

        })).addPathPatterns("/**");
    }
}
