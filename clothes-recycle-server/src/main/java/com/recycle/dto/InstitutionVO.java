package com.recycle.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 机构列表响应 VO
 * 合并 user 表和 institution 表信息
 */
@Data
public class InstitutionVO {

    /** 机构扩展表ID */
    private Long id;

    /** 关联的用户ID */
    private Long userId;

    /** 手机号（来自 user 表） */
    private String phone;

    /** 机构名称 */
    private String name;

    /** 机构地址 */
    private String address;

    /** 联系人 */
    private String contactPerson;

    /** 机构类型 */
    private String type;

    /** 状态：1=正常, 0=禁用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
