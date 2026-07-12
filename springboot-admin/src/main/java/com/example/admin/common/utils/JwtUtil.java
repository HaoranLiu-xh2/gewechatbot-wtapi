package com.example.admin.common.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 *
 * @author example
 */
@Slf4j
@Component
public class JwtUtil {

    /**
     * JWT 密钥
     */
    private static String SECRET;

    /**
     * Token 有效期（毫秒）
     */
    private static Long EXPIRATION;

    @Value("${jwt.secret}")
    public void setSecret(String secret) {
        JwtUtil.SECRET = secret;
    }

    @Value("${jwt.expiration}")
    public void setExpiration(Long expiration) {
        JwtUtil.EXPIRATION = expiration;
    }

    /**
     * 获取加密密钥
     */
    private static SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT Token
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @return Token 字符串
     */
    public static String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>(2);
        claims.put("userId", userId);
        claims.put("username", username);
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION);
        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSecretKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 解析 JWT Token
     *
     * @param token Token 字符串
     * @return 解析后的 Claims
     */
    public static Claims parseToken(String token) {
        if (StrUtil.isBlank(token)) {
            return null;
        }
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.error("JWT Token 已过期：{}", e.getMessage());
            return null;
        } catch (UnsupportedJwtException | MalformedJwtException | IllegalArgumentException e) {
            log.error("JWT Token 解析失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 Token 中获取用户 ID
     */
    public static Long getUserId(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        return Long.valueOf(claims.get("userId").toString());
    }

    /**
     * 从 Token 中获取用户名
     */
    public static String getUsername(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        return claims.get("username", String.class);
    }

    /**
     * 判断 Token 是否过期
     */
    public static boolean isExpired(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return true;
        }
        return claims.getExpiration().before(new Date());
    }

    /**
     * 获取 Token 过期时间字符串
     */
    public static String getExpirationTime(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return "";
        }
        return DateUtil.formatDateTime(claims.getExpiration());
    }
}
