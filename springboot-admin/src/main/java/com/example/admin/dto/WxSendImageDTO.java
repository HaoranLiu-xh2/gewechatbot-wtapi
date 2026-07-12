package com.example.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 发送图片消息请求参数
 *
 * @author example
 */
@Data
public class WxSendImageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 设备 ID
     */
    @NotBlank(message = "appId 不能为空")
    private String appId;

    /**
     * 接收人 wxid
     */
    @NotBlank(message = "toWxid 不能为空")
    private String toWxid;

    /**
     * 图片 URL（已上传至对象存储）
     */
    @NotBlank(message = "图片 URL 不能为空")
    private String imgUrl;
}
