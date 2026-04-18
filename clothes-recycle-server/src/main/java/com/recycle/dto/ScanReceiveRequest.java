package com.recycle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 机构扫码接收请求 DTO
 * 机构扫描回收员生成的溯源二维码后，提交订单编号完成接收
 */
@Data
@Schema(description = "机构扫码接收请求")
public class ScanReceiveRequest {

    /** 订单编号（从二维码扫描获得） */
    @Schema(description = "订单编号（从二维码扫描获得）", example = "RC202604131234560001")
    private String orderNo;
}
