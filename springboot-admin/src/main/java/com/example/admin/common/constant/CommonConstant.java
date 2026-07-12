package com.example.admin.common.constant;

/**
 * 公共常量类
 *
 * @author example
 */
public class CommonConstant {

    private CommonConstant() {
    }

    /**
     * 用户状态：正常
     */
    public static final Integer USER_STATUS_ENABLE = 1;

    /**
     * 用户状态：禁用
     */
    public static final Integer USER_STATUS_DISABLE = 0;

    /**
     * 逻辑删除：未删除
     */
    public static final Integer DELETED_NO = 0;

    /**
     * 逻辑删除：已删除
     */
    public static final Integer DELETED_YES = 1;

    /**
     * Token 请求头名称
     */
    public static final String TOKEN_HEADER = "Authorization";

    /**
     * Token 前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 登录接口路径
     */
    public static final String LOGIN_PATH = "/api/login";

    /**
     * 注册接口路径
     */
    public static final String REGISTER_PATH = "/api/register";
}
