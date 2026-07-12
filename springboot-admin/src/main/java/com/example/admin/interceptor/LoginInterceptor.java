package com.example.admin.interceptor;

import cn.hutool.core.util.StrUtil;
import com.example.admin.common.constant.CommonConstant;
import com.example.admin.common.exception.BusinessException;
import com.example.admin.common.result.ResultCode;
import com.example.admin.common.utils.JwtUtil;
import com.example.admin.common.utils.UserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录认证拦截器
 *
 * @author example
 */
@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 获取请求路径
        String uri = request.getRequestURI();

        // 登录和注册接口放行
        if (CommonConstant.LOGIN_PATH.equals(uri) || CommonConstant.REGISTER_PATH.equals(uri)) {
            return true;
        }

        // 从请求头中获取 Token
        String authHeader = request.getHeader(CommonConstant.TOKEN_HEADER);
        if (StrUtil.isBlank(authHeader) || !authHeader.startsWith(CommonConstant.TOKEN_PREFIX)) {
            log.warn("请求未携带 Token，URI：{}", uri);
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 截取 Token
        String token = authHeader.substring(CommonConstant.TOKEN_PREFIX.length());
        if (StrUtil.isBlank(token)) {
            log.warn("Token 为空，URI：{}", uri);
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 解析 Token
        Claims claims = JwtUtil.parseToken(token);
        if (claims == null) {
            log.warn("Token 解析失败或已过期，URI：{}", uri);
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 设置当前登录用户到上下文
        Long userId = JwtUtil.getUserId(token);
        String username = JwtUtil.getUsername(token);
        UserContext.setUser(userId, username);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束后清理上下文，防止线程复用导致数据错乱
        UserContext.clear();
    }
}
