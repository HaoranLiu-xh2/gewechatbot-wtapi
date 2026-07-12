package com.example.admin.websocket;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天 WebSocket 处理器
 *
 * @author example
 */
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    /**
     * 用户 ID -> WebSocket 会话映射
     */
    private static final Map<Long, WebSocketSession> USER_SESSION_MAP = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserId(session);
        if (userId == null) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        USER_SESSION_MAP.put(userId, session);
        log.info("WebSocket 连接建立：userId={}", userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = getUserId(session);
        log.debug("收到 WebSocket 消息：userId={}，payload={}", userId, message.getPayload());
        // 目前服务端只负责推送，前端心跳等消息可在此扩展
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserId(session);
        if (userId != null) {
            USER_SESSION_MAP.remove(userId);
            log.info("WebSocket 连接关闭：userId={}，status={}", userId, status);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        Long userId = getUserId(session);
        log.error("WebSocket 传输异常：userId={}", userId, exception);
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    /**
     * 推送消息给指定用户
     *
     * @param userId  用户 ID
     * @param message 消息对象
     */
    public static void sendMessage(Long userId, Object message) {
        WebSocketSession session = USER_SESSION_MAP.get(userId);
        if (session == null || !session.isOpen()) {
            log.debug("用户 {} 不在线，无法推送 WebSocket 消息", userId);
            return;
        }
        try {
            String payload = JSON.toJSONString(message);
            session.sendMessage(new TextMessage(payload));
            log.debug("WebSocket 消息已推送：userId={}，payload={}", userId, payload);
        } catch (IOException e) {
            log.error("WebSocket 消息推送失败：userId={}", userId, e);
        }
    }

    /**
     * 从会话属性中获取用户 ID
     */
    private Long getUserId(WebSocketSession session) {
        Object userIdObj = session.getAttributes().get("userId");
        if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        }
        return null;
    }
}
