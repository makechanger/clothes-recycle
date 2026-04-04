package com.recycle.dto;

import lombok.Data;

/**
 * 用户地址请求 DTO
 * 用于新增和编辑地址时的请求参数
 */
@Data
public class UserAddressRequest {

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

    /** 是否设为默认地址：1=是, 0=否 */
    private Integer isDefault;
}
