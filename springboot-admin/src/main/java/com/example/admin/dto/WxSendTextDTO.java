package com.example.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 发送文本消息请求参数
 *
 * @author example
 */
@Data
public class WxSendTextDTO implements Serializable {

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
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String content;
}
