package com.example.admin.common.result;

import lombok.Getter;

/**
 * 统一响应状态码枚举
 *
 * @author example
 */
@Getter
public enum ResultCode {

    /**
     * 操作成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 操作失败
     */
    ERROR(500, "操作失败"),

    /**
     * 参数错误
     */
    PARAM_ERROR(400, "参数错误"),

    /**
     * 未登录或登录已过期
     */
    UNAUTHORIZED(401, "未登录或登录已过期"),

    /**
     * 无权限访问
     */
    FORBIDDEN(403, "无权限访问"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * 用户名或密码错误
     */
    LOGIN_ERROR(1001, "用户名或密码错误"),

    /**
     * 用户名已存在
     */
    USERNAME_EXIST(1002, "用户名已存在"),

    /**
     * 用户不存在
     */
    USER_NOT_FOUND(1003, "用户不存在");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 状态消息
     */
    private final String msg;

    ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
