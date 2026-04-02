package com.recycle.controller;

import com.recycle.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试接口 - 用于验证项目是否正常启动
 * 验证通过后可删除
 */
@Tag(name = "测试接口")
@RestController
public class TestController {

    @Operation(summary = "连通性测试")
    @GetMapping("/api/test")
    public Result<String> test() {
        return Result.success("hello");
    }
}
