package com.recycle.dto;

import lombok.Data;

/**
 * 修改密码请求 DTO
 * 三种角色（用户/回收员/机构）共用同一个请求格式
 */
@Data
public class ChangePasswordRequest {

    /** 旧密码（用于验证身份） */
    private String oldPassword;

    /** 新密码（至少6位） */
    private String newPassword;
}
