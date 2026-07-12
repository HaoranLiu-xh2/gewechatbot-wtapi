package com.example.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 获取微信登录二维码请求参数
 *
 * @author example
 */
@Data
public class WxQrCodeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 微信应用 ID（首次不传）
     */
    private String appId;

    /**
     * 辅助 ID
     */
    private String aid;

    /**
     * 代理 IP
     */
    private String proxyIp;

    /**
     * 地区编码
     */
    @NotBlank(message = "地区编码不能为空")
    private String regionId;

    /**
     * 登录方式，默认 mac
     */
    private String type = "mac";
}
