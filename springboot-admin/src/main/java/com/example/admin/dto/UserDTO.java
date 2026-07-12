package com.example.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户新增/修改请求参数
 *
 * @author example
 */
@Data
public class UserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID（新增时为空，修改时必填）
     */
    private Long id;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在 3-50 个字符之间")
    private String username;

    /**
     * 密码（新增时必填，修改时为空表示不修改密码）
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 邮箱
     */
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", message = "邮箱格式不正确")
    private String email;

    /**
     * 微信 API Token（X-finder-TOKEN）
     */
    private String token;

    /**
     * 状态：1-正常，0-禁用
     */
    private Integer status;
}
