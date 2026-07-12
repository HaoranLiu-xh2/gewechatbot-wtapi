package com.example.admin.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一接口返回结果
 *
 * @param <T> 返回数据类型
 * @author example
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应状态码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 响应数据
     */
    private T data;

    public Result() {
    }

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 成功返回结果
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), null);
    }

    /**
     * 成功返回结果（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), data);
    }

    /**
     * 成功返回结果（带消息，无数据）
     */
    public static Result<Void> success(String msg) {
        return new Result<>(ResultCode.SUCCESS.getCode(), msg, null);
    }

    /**
     * 成功返回结果（带消息和数据）
     */
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), msg, data);
    }

    /**
     * 失败返回结果
     */
    public static <T> Result<T> error() {
        return new Result<>(ResultCode.ERROR.getCode(), ResultCode.ERROR.getMsg(), null);
    }

    /**
     * 失败返回结果（带消息）
     */
    public static <T> Result<T> error(String msg) {
        return new Result<>(ResultCode.ERROR.getCode(), msg, null);
    }

    /**
     * 失败返回结果（带状态码和消息）
     */
    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }

    /**
     * 根据结果码返回结果
     */
    public static <T> Result<T> result(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMsg(), null);
    }
}
