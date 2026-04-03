package com.recycle.dto;

import lombok.Data;

import java.util.Map;

/**
 * 统一登录响应
 * 返回 token、角色、用户基本信息
 */
@Data
public class LoginResponse {

    /** 登录 token */
    private String token;

    /** 角色：USER / COLLECTOR / INSTITUTION */
    private String role;

    /** 用户基本信息（id、name、phone 等） */
    private Map<String, Object> userInfo;
}
