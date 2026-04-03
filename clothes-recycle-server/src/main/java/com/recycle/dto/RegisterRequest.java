package com.recycle.dto;

import lombok.Data;

/**
 * 用户注册请求参数
 * 所有角色先注册为普通用户，后续通过资质申请升级角色
 */
@Data
public class RegisterRequest {

    /** 手机号（11位） */
    private String phone;

    /** 密码 */
    private String password;

    /** 用户姓名 */
    private String name;
}
