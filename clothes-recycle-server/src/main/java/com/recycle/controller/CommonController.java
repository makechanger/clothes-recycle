package com.recycle.controller;

import com.recycle.common.Result;
import com.recycle.entity.Community;
import com.recycle.mapper.CommunityMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 公共接口控制器
 * 路由前缀 /api/common，无需登录即可访问
 * 在 SaTokenConfig 中已放行 /api/common/**
 */
@Tag(name = "公共接口")
@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
public class CommonController {

    private final CommunityMapper communityMapper;

    /**
     * 获取社区列表
     * 返回所有社区信息，供用户注册或下单时选择
     */
    @Operation(summary = "获取社区列表")
    @GetMapping("/community/list")
    public Result<List<Community>> communityList() {
        List<Community> list = communityMapper.selectList(null);
        return Result.success(list);
    }
}
