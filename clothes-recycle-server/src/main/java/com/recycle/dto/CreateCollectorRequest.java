package com.recycle.dto;

import lombok.Data;

/**
 * 管理员创建回收员请求参数
 * 管理员在后台直接创建回收员账号
 */
@Data
public class CreateCollectorRequest {

    /** 手机号（11位） */
    private String phone;

    /** 密码 */
    private String password;

    /** 回收员姓名 */
    private String name;
}
