package com.recycle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员实体类
 * 对应数据库 admin 表，用于后台管理系统登录
 */
@Data
@TableName("admin")
public class Admin {

    /** 管理员ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录用户名 */
    private String username;

    /** 密码（加密存储） */
    private String passwordHash;

    /** 角色：admin=超级管理员, operator=运营 */
    private String role;

    /** 状态：1=正常, 0=禁用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
