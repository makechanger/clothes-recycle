package com.recycle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置
 * 开发阶段允许所有来源访问，解决前端（小程序/管理后台）调用后端接口时的跨域问题
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许所有域名访问（开发阶段，上线后应改为具体域名）
        config.addAllowedOriginPattern("*");
        // 允许携带 Cookie / Token
        config.setAllowCredentials(true);
        // 允许所有请求头（包括自定义的 Authorization 头）
        config.addAllowedHeader("*");
        // 允许所有 HTTP 方法（GET、POST、PUT、DELETE 等）
        config.addAllowedMethod("*");

        // 对所有路径生效
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
