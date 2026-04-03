package com.recycle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色申请实体类
 * 对应数据库 role_application 表
 * 用户申请成为回收员或机构，管理员审批
 */
@Data
@TableName("role_application")
public class RoleApplication {

    /** 申请ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 申请用户ID */
    private Long userId;

    /** 申请角色：COLLECTOR=回收员, INSTITUTION=机构 */
    private String applyRole;

    /** 真实姓名/机构名称 */
    private String name;

    /** 身份证照片URL（回收员必填） */
    private String idCardPhoto;

    /** 健康证照片URL（回收员选填） */
    private String healthCertPhoto;

    /** 机构地址（机构必填） */
    private String address;

    /** 联系人（机构必填） */
    private String contactPerson;

    /** 审核状态：0=待审核, 1=已通过, 2=已拒绝 */
    private Integer status;

    /** 拒绝原因 */
    private String rejectReason;

    /** 申请时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
