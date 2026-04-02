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
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            
            // 1. 公共放行路由（不需要任何登录）
            SaRouter.match("/**")
                    .notMatch(
                            "/api/user/login",          // 用户登录
                            "/api/collector/login",     // 回收员登录
                            "/api/institution/login",   // 机构登录
                            "/api/admin/login",         // 管理员登录
                            "/api/common/**",           // 公共接口（如文件上传）
                            "/api/test",                // 测试接口
                            "/doc.html",                // Knife4j 文档页面
                            "/webjars/**",              // Knife4j 静态资源
                            "/v3/api-docs/**",          // OpenAPI 数据接口
                            "/swagger-ui/**",           // Swagger UI
                            "/favicon.ico"
                    );

            // 2. 严格角色隔离鉴权
            
            // 只有【用户】能访问的接口
            SaRouter.match("/api/user/**")
                    .notMatch("/api/user/login") 
                    .check(r -> StpUtil.checkLogin()); // 默认体系作为普通用户

            // 只有【回收员】能访问的接口
            SaRouter.match("/api/collector/**")
                    .notMatch("/api/collector/login")
                    .check(r -> new StpLogic("collector").checkLogin());

            // 只有【机构】能访问的接口
            SaRouter.match("/api/institution/**")
                    .notMatch("/api/institution/login")
                    .check(r -> new StpLogic("institution").checkLogin());

            // 只有【管理员】能访问的接口
            SaRouter.match("/api/admin/**")
                    .notMatch("/api/admin/login")
                    .check(r -> new StpLogic("admin").checkLogin());

        })).addPathPatterns("/**");
    }
}