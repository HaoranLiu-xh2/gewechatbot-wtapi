package com.example.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信退出登录请求参数
 *
 * @author example
 */
@Data
public class WxLogoutDTO {

    /**
     * 微信应用 ID
     */
    @NotBlank(message = "appId 不能为空")
    private String appId;
}
