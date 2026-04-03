package com.recycle.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 机构端控制器
 * 处理机构特有的业务操作
 * 路由前缀 /api/institution，由 SaTokenConfig 中 StpUtil 鉴权保护
 *
 * 注意：修改密码已统一到 /api/user/changePassword（所有角色通用）
 * 后续机构特有的业务接口在此添加
 */
@Tag(name = "机构端接口")
@RestController
@RequestMapping("/api/institution")
@RequiredArgsConstructor
public class InstitutionController {

    // 后续添加机构特有的业务接口
}
