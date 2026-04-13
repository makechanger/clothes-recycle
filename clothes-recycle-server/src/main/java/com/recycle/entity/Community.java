package com.recycle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 社区实体类
 * 对应数据库 community 表，记录平台管理的社区信息
 */
@Data
@TableName("community")
public class Community {

    /** 社区ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 社区名称 */
    private String name;

    /** 省份 */
    private String province;

    /** 城市 */
    private String city;

    /** 区/县 */
    private String district;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
