package com.example.admin.config;

import com.example.admin.common.utils.JwtUtil;
import com.example.admin.websocket.ChatWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 配置类
 *
 * @author example
 */
@Slf4j
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler(), "/ws/chat")
                .addInterceptors(chatHandshakeInterceptor())
                .setAllowedOrigins("*");
    }

    @Bean
    public ChatWebSocketHandler chatWebSocketHandler() {
        return new ChatWebSocketHandler();
    }

    @Bean
    public HandshakeInterceptor chatHandshakeInterceptor() {
        return new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(org.springframework.http.server.ServerHttpRequest request,
                                           org.springframework.http.server.ServerHttpResponse response,
                                           org.springframework.web.socket.WebSocketHandler wsHandler,
                                           Map<String, Object> attributes) {
                String token = null;
                String query = request.getURI().getQuery();
                if (query != null && query.contains("token=")) {
                    for (String param : query.split("&")) {
                        if (param.startsWith("token=")) {
                            token = param.substring("token=".length());
                            break;
                        }
                    }
                }
                if (token == null || JwtUtil.parseToken(token) == null) {
                    log.warn("WebSocket 握手失败：Token 无效");
                    return false;
                }
                Long userId = JwtUtil.getUserId(token);
                attributes.put("userId", userId);
                return true;
            }

            @Override
            public void afterHandshake(org.springframework.http.server.ServerHttpRequest request,
                                       org.springframework.http.server.ServerHttpResponse response,
                                       org.springframework.web.socket.WebSocketHandler wsHandler,
                                       Exception exception) {
                // 握手后无需处理
            }
        };
    }
}
