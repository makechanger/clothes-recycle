package com.recycle.common;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 全局异常处理器
 * 拦截所有 Controller 层抛出的异常，统一返回 Result 格式的错误响应
 * 避免前端收到原始的 500 错误页面
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Sa-Token 未登录异常
     * 触发场景：访问需要鉴权的接口但未携带有效 token
     */
    @ExceptionHandler(NotLoginException.class)
    public Result<?> handleNotLogin(NotLoginException e) {
        log.warn("未登录访问: {}", e.getMessage());
        return Result.error(401, "未登录或登录已过期，请重新登录");
    }

    /**
     * Sa-Token 无权限异常
     * 触发场景：已登录但无权访问该接口
     */
    @ExceptionHandler(NotPermissionException.class)
    public Result<?> handleNotPermission(NotPermissionException e) {
        log.warn("无权限访问: {}", e.getMessage());
        return Result.error(403, "无权限执行此操作");
    }

    /**
     * 文件上传超过大小限制
     * 触发场景：上传文件超过 application.yml 中配置的 5MB 限制
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<?> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        log.warn("文件上传超限: {}", e.getMessage());
        return Result.error(400, "上传文件大小不能超过5MB");
    }

    /**
     * 自定义业务异常
     * 触发场景：业务逻辑中手动抛出的异常（如参数校验失败、业务规则不满足）
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusiness(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常（IllegalArgumentException）
     * 触发场景：方法参数不合法
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("参数异常: {}", e.getMessage());
        return Result.error(400, e.getMessage());
    }

    /**
     * 兜底：所有未被上面捕获的异常
     * 避免前端看到 500 错误页面，统一返回 JSON 格式
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.error(500, "服务器内部错误，请稍后再试");
    }
}
