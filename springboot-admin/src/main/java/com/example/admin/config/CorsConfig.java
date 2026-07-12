package com.example.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置类
 *
 * @author example
 */
@Configuration
public class CorsConfig {

    /**
     * 配置跨域过滤器
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许任何来源
        config.addAllowedOriginPattern("*");
        // 允许携带凭证
        config.setAllowCredentials(true);
        // 允许任何请求头
        config.addAllowedHeader("*");
        // 允许任何请求方法
        config.addAllowedMethod("*");
        // 暴露响应头
        config.addExposedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
