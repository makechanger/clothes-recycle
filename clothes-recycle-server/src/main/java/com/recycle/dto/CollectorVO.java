package com.recycle.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 回收员列表响应 VO
 * 合并 user 表和 collector 表信息，避免暴露密码等敏感字段
 */
@Data
public class CollectorVO {

    /** 回收员扩展表ID */
    private Long id;

    /** 关联的用户ID */
    private Long userId;

    /** 手机号（来自 user 表） */
    private String phone;

    /** 回收员姓名 */
    private String name;

    /** 身份证照片URL */
    private String idCardPhoto;

    /** 资质状态：0=待完善, 1=待审核, 2=已认证, 3=已禁用 */
    private Integer collectorStatus;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
