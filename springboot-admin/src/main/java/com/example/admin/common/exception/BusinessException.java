package com.example.admin.common.exception;

import com.example.admin.common.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常
 *
 * @author example
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 异常状态码
     */
    private final Integer code;

    public BusinessException(String msg) {
        super(msg);
        this.code = ResultCode.ERROR.getCode();
    }

    public BusinessException(Integer code, String msg) {
        super(msg);
        this.code = code;
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
    }
}
