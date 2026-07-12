package com.example.admin.config;

import com.example.admin.interceptor.LoginInterceptor;
import com.example.admin.interceptor.WebLogInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 *
 * @author example
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;
    private final WebLogInterceptor webLogInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册接口日志拦截器（拦截所有请求）
        registry.addInterceptor(webLogInterceptor)
                .addPathPatterns("/**");

        // 注册登录认证拦截器（拦截所有请求，登录注册、公开回调及 WebSocket 握手除外）
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/api/login", "/api/register", "/api/wx/message/callback", "/ws/**");
    }
}
