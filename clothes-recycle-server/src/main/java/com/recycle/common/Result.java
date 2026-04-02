package com.recycle.common;

import lombok.Data;

/**
 * 统一响应封装类
 * 所有接口统一返回此格式：{ code: 200, message: "success", data: ... }
 *
 * @param <T> 响应数据的类型
 */
@Data
public class Result<T> {

    /** 状态码：200 成功，其他为失败 */
    private int code;

    /** 提示信息 */
    private String message;

    /** 响应数据 */
    private T data;

    // ===== 快捷构造方法 =====

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> error(String message) {
        return error(500, message);
    }
}
