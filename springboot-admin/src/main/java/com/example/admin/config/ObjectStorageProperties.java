package com.example.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 对象存储配置属性
 *
 * @author example
 */
@Data
@Component
@ConfigurationProperties(prefix = "object-storage")
public class ObjectStorageProperties {

    /**
     * 存储类型：oss
     */
    private String type = "oss";

    /**
     * 阿里云 OSS 配置
     */
    private Oss oss = new Oss();

    @Data
    public static class Oss {

        /**
         * 访问站点，如 oss-cn-beijing.aliyuncs.com
         */
        private String endPoint;

        /**
         * 自定义域名（可选）
         */
        private String domain;

        /**
         * 文件前缀（可选）
         */
        private String prefix;

        /**
         * AccessKeyId
         */
        private String accessKeyId;

        /**
         * AccessKeySecret
         */
        private String accessKeySecret;

        /**
         * 存储空间名
         */
        private String bucket;

        /**
         * 存储区域，为空时自动从 endpoint 解析
         */
        private String region;

        /**
         * 是否使用 HTTPS（Y=是，N=否）
         */
        private String isHttps = "N";

        /**
         * 桶权限类型（0=private 1=public 2=custom）
         */
        private String accessPolicy = "1";
    }
}
