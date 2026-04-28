package com.recycle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 机构扩展信息实体类
 * 对应数据库 institution 表，通过 user_id 关联 user 表
 * 存储机构特有的信息（地址、联系人等）
 */
@Data
@TableName("institution")
public class Institution {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的用户ID（对应 user 表的 id） */
    private Long userId;

    /** 机构名称 */
    private String name;

    /** 机构地址 */
    private String address;

    /** 联系人 */
    private String contactPerson;

    /** 机构类型：DONATION=捐赠机构, RECYCLE=再生利用, ENVIRONMENTAL=环保处理 */
    private String type;

    /** 状态：1=正常, 0=禁用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
