package com.example.admin.interceptor;

import com.alibaba.fastjson2.JSON;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Enumeration;

/**
 * 接口请求日志拦截器
 * 记录请求地址、请求参数、请求头、耗时、返回结果和异常信息
 *
 * @author example
 */
@Slf4j
@Component
public class WebLogInterceptor implements HandlerInterceptor {

    /**
     * 请求开始时间线程变量
     */
    private static final ThreadLocal<Long> START_TIME = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 记录请求开始时间
        START_TIME.set(System.currentTimeMillis());

        String uri = request.getRequestURI();
        String method = request.getMethod();

        // 记录请求参数
        String params = JSON.toJSONString(request.getParameterMap());

        // 记录请求头
        StringBuilder headers = new StringBuilder();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames != null && headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.append(headerName).append("=").append(request.getHeader(headerName)).append("; ");
        }

        log.info("请求开始，URI：{}，Method：{}，参数：{}，Headers：{}", uri, method, params, headers);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 计算请求耗时
        Long startTime = START_TIME.get();
        long cost = startTime == null ? 0 : System.currentTimeMillis() - startTime;

        String uri = request.getRequestURI();
        String method = request.getMethod();
        int status = response.getStatus();

        if (ex != null) {
            log.error("请求异常，URI：{}，Method：{}，状态码：{}，耗时：{}ms，异常：{}",
                    uri, method, status, cost, ex.getMessage(), ex);
        } else {
            log.info("请求结束，URI：{}，Method：{}，状态码：{}，耗时：{}ms", uri, method, status, cost);
        }

        // 清理线程变量
        START_TIME.remove();
    }
}
