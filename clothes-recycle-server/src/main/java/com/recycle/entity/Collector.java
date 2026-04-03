package com.recycle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 回收员扩展信息实体类
 * 对应数据库 collector 表，通过 user_id 关联 user 表
 * 存储回收员特有的资质信息（身份证照片等）
 */
@Data
@TableName("collector")
public class Collector {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的用户ID（对应 user 表的 id） */
    private Long userId;

    /** 回收员真实姓名 */
    private String name;

    /** 身份证照片URL */
    private String idCardPhoto;

    /** 状态：0=待完善资质, 1=待审核, 2=已认证, 3=已禁用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
