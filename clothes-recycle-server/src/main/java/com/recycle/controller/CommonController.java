package com.recycle.controller;

import com.recycle.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 公共接口控制器
 * 路由前缀 /api/common，无需登录即可访问
 * 在 SaTokenConfig 中已放行 /api/common/**
 */
@Tag(name = "公共接口")
@RestController
@RequestMapping("/api/common")
public class CommonController {

    /** 文件上传存储路径，从 application.yml 读取 */
    @Value("${file.upload-path:./uploads/}")
    private String uploadPath;

    /**
     * 通用文件上传接口
     * 保存到本地 uploads 目录，返回可访问的 URL 路径
     * 文件大小限制 5MB（在 application.yml 中配置）
     * 仅允许上传图片格式（jpg/jpeg/png/gif/webp）
     *
     * @param file 上传的文件
     * @return 文件访问 URL（如 /uploads/xxx.jpg）
     */
    @Operation(summary = "文件上传")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        // 校验文件不为空
        if (file.isEmpty()) {
            return Result.error(400, "上传文件不能为空");
        }

        // 校验文件类型（仅允许图片）
        String originalFilename = file.getOriginalFilename();
        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }
        if (!".jpg".equals(suffix) && !".jpeg".equals(suffix) && !".png".equals(suffix)
                && !".gif".equals(suffix) && !".webp".equals(suffix)) {
            return Result.error(400, "仅支持上传图片文件（jpg/jpeg/png/gif/webp）");
        }

        // 生成唯一文件名，避免重名覆盖
        String newFilename = UUID.randomUUID().toString().replace("-", "") + suffix;

        // 确保上传目录存在（转为绝对路径，避免解析到 Tomcat 临时目录）
        File uploadDir = new File(uploadPath).getAbsoluteFile();
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 保存文件到本地磁盘
        File destFile = new File(uploadDir, newFilename);
        file.transferTo(destFile.getAbsoluteFile());

        // 返回可通过 HTTP 访问的相对路径
        String url = "/uploads/" + newFilename;
        return Result.success(url);
    }
}
