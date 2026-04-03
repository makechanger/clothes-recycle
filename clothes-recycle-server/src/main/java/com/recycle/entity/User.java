package com.recycle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库 user 表，普通用户使用手机号+密码登录
 */
@Data
@TableName("`user`")
public class User {

    /** 用户ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 手机号（登录凭证） */
    private String phone;

    /** 密码（加密存储） */
    private String passwordHash;

    /** 用户姓名 */
    private String name;

    /** 角色：USER-普通用户 / COLLECTOR-回收员 / INSTITUTION-机构 */
    private String role;

    /** 所属社区ID */
    private Long communityId;

    /** 积分余额 */
    private Integer pointsBalance;

    /** 状态：1=正常, 0=禁用, 2=已注销 */
    private Integer status;

    /** 注册时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
