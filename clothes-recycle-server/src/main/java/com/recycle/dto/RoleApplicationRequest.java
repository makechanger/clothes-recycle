package com.recycle.dto;

import lombok.Data;

/**
 * 资质申请请求 DTO
 * 用户提交申请成为回收员或机构
 */
@Data
public class RoleApplicationRequest {

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
}
