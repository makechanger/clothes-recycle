package com.recycle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户地址实体类
 * 对应数据库 user_address 表，存储用户的上门回收地址
 */
@Data
@TableName("user_address")
public class UserAddress {

    /** 地址ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID */
    private Long userId;

    /** 联系人姓名 */
    private String name;

    /** 联系电话 */
    private String phone;

    /** 省份 */
    private String province;

    /** 城市 */
    private String city;

    /** 区/县 */
    private String district;

    /** 详细地址 */
    private String detailAddress;

    /** 是否默认地址：1=是, 0=否 */
    private Integer isDefault;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
