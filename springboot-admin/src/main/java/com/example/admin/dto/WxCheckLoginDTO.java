package com.example.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 检查微信登录状态请求参数
 *
 * @author example
 */
@Data
public class WxCheckLoginDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 微信应用 ID
     */
    @NotBlank(message = "appId 不能为空")
    private String appId;

    /**
     * 登录 UUID
     */
    @NotBlank(message = "uuid 不能为空")
    private String uuid;

    /**
     * 是否自动滑块
     */
    private Boolean autoSliding = true;

    /**
     * 地区编码
     */
    private String regionId;

    /**
     * 地区名称
     */
    private String regionName;

    /**
     * 登录方式
     */
    private String type = "mac";
}
