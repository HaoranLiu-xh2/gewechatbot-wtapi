package com.example.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 发送视频消息请求参数
 *
 * @author example
 */
@Data
public class WxSendVideoDTO implements Serializable {

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
     * 视频 URL（已上传至对象存储）
     */
    @NotBlank(message = "视频 URL 不能为空")
    private String videoUrl;

    /**
     * 视频封面 URL
     */
    private String thumbUrl;

    /**
     * 视频时长（秒）
     */
    @NotNull(message = "视频时长不能为空")
    private Integer videoDuration;
}
