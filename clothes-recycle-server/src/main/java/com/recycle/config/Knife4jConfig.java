package com.recycle.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j（Swagger）接口文档配置
 * 启动后访问 http://localhost:8080/doc.html 查看自动生成的 API 文档
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("衣物回收平台 API")
                        .description("衣物回收平台后端接口文档")
                        .version("1.0.0"));
    }
}
