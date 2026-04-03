package com.recycle.dto;

import lombok.Data;

/**
 * 统一登录请求参数
 * 三种角色（用户/回收员/机构）都使用手机号+密码登录
 */
@Data
public class LoginRequest {

    /** 手机号 */
    private String phone;

    /** 密码 */
    private String password;
}
