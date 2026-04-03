package com.recycle.dto;

import lombok.Data;

/**
 * 管理员登录请求参数
 * 管理员使用用户名+密码登录（与普通用户的手机号登录区分）
 */
@Data
public class AdminLoginRequest {

    /** 登录用户名 */
    private String username;

    /** 登录密码 */
    private String password;
}
