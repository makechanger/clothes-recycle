package com.recycle.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 回收员端控制器
 * 处理回收员特有的业务操作
 * 路由前缀 /api/collector，由 SaTokenConfig 中 StpUtil 鉴权保护
 *
 * 注意：修改密码已统一到 /api/user/changePassword（所有角色通用）
 * 后续回收员特有的业务接口（如接单、完成订单等）在此添加
 */
@Tag(name = "回收员端接口")
@RestController
@RequestMapping("/api/collector")
@RequiredArgsConstructor
public class CollectorController {

    // 后续添加回收员特有的业务接口
}
