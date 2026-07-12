package com.example.admin.common.oss;

import lombok.Builder;
import lombok.Data;

/**
 * 对象存储上传返回体
 *
 * @author example
 */
@Data
@Builder
public class UploadResult {

    /**
     * 文件访问 URL
     */
    private String url;

    /**
     * 文件在存储桶中的 key
     */
    private String filename;

    /**
     * 已上传对象的 ETag
     */
    private String eTag;
}
