package com.example.admin.common.exception;

import com.example.admin.common.result.Result;
import com.example.admin.common.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * @author example
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.error("业务异常，请求地址：{}，异常信息：{}", request.getRequestURI(), e.getMessage(), e);
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常（@RequestBody 形式）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(","));
        log.error("参数校验异常，请求地址：{}，异常信息：{}", request.getRequestURI(), msg);
        return Result.error(ResultCode.PARAM_ERROR.getCode(), msg);
    }

    /**
     * 处理参数绑定异常（表单形式）
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e, HttpServletRequest request) {
        String msg = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(","));
        log.error("参数绑定异常，请求地址：{}，异常信息：{}", request.getRequestURI(), msg);
        return Result.error(ResultCode.PARAM_ERROR.getCode(), msg);
    }

    /**
     * 处理约束校验异常（@RequestParam / @PathVariable 形式）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        String msg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(","));
        log.error("约束校验异常，请求地址：{}，异常信息：{}", request.getRequestURI(), msg);
        return Result.error(ResultCode.PARAM_ERROR.getCode(), msg);
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("运行时异常，请求地址：{}，异常信息：{}", request.getRequestURI(), e.getMessage(), e);
        return Result.error(e.getMessage());
    }

    /**
     * 处理其他所有异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常，请求地址：{}，异常信息：{}", request.getRequestURI(), e.getMessage(), e);
        return Result.error("系统繁忙，请稍后重试");
    }
}
