package com.example.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 下载图片请求 DTO
 *
 * @author example
 */
@Data
public class WxDownloadImageDTO {

    /**
     * 本地消息 ID
     */
    @NotNull(message = "消息 ID 不能为空")
    private Long messageId;

    /**
     * 下载类型：1-高清原图，2-常规图片，3-缩略图
     */
    @NotNull(message = "下载类型不能为空")
    private Integer type;
}
