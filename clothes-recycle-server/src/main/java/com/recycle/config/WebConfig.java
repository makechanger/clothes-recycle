package com.recycle.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 静态资源映射配置
 * 将本地 uploads 目录映射为可通过 HTTP 访问的静态资源路径
 * 例如：上传到 ./uploads/abc.jpg 的文件可通过 http://localhost:8080/uploads/abc.jpg 访问
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-path:./uploads/}")
    private String uploadPath;

@Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    String path = uploadPath;
    if (!path.endsWith("/")) {
        path += "/";
    }
    registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:" + path);
}
}
