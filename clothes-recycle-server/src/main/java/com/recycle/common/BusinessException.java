package com.recycle.common;

import lombok.Getter;

/**
 * 自定义业务异常
 * 在 Service 层中抛出，由 GlobalExceptionHandler 统一捕获并返回给前端
 * 用法：throw new BusinessException("手机号已被绑定");
 *       throw new BusinessException(400, "每日取消订单不能超过3次");
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 错误码，默认 500 */
    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
