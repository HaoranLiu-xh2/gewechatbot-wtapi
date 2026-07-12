package com.example.admin.common.utils;

/**
 * 当前登录用户上下文
 *
 * @author example
 */
public class UserContext {

    /**
     * 当前登录用户 ID 线程变量
     */
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    /**
     * 当前登录用户名线程变量
     */
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();

    private UserContext() {
    }

    /**
     * 设置当前登录用户
     */
    public static void setUser(Long userId, String username) {
        USER_ID.set(userId);
        USERNAME.set(username);
    }

    /**
     * 获取当前登录用户 ID
     */
    public static Long getUserId() {
        return USER_ID.get();
    }

    /**
     * 获取当前登录用户名
     */
    public static String getUsername() {
        return USERNAME.get();
    }

    /**
     * 清除当前登录用户信息
     */
    public static void clear() {
        USER_ID.remove();
        USERNAME.remove();
    }
}
