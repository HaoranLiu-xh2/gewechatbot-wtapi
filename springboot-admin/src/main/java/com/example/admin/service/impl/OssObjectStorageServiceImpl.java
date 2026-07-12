package com.example.admin.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.example.admin.common.exception.BusinessException;
import com.example.admin.config.ObjectStorageProperties;
import com.example.admin.service.ObjectStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 对象存储服务实现（基于 AWS SDK 2.x 兼容 S3 协议）
 * 支持阿里云 OSS、腾讯云 COS、七牛云、MinIO 等
 *
 * @author example
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "object-storage", name = "type", havingValue = "oss", matchIfMissing = true)
public class OssObjectStorageServiceImpl implements ObjectStorageService {

    /**
     * 已知云服务商 endpoint 标识
     */
    private static final List<String> CLOUD_SERVICES = Arrays.asList("aliyun", "qcloud", "qiniu", "obs", "myqcloud", "qiniucs", "sdqd");

    private final ObjectStorageProperties properties;

    @Override
    public String upload(MultipartFile file) {
        return upload(file, "wx");
    }

    @Override
    public String upload(MultipartFile file, String prefix) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        ObjectStorageProperties.Oss oss = properties.getOss();
        if (oss == null || StrUtil.isBlank(oss.getAccessKeyId()) || StrUtil.isBlank(oss.getAccessKeySecret())
            || StrUtil.isBlank(oss.getEndPoint()) || StrUtil.isBlank(oss.getBucket())) {
            throw new BusinessException("对象存储配置不完整");
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = "";
        if (StrUtil.isNotBlank(originalFilename) && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = buildKey(prefix, oss.getPrefix(), dateDir, IdUtil.fastSimpleUUID() + suffix);

        try (S3Client s3Client = buildClient(oss)) {
            PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                .bucket(cleanBackticks(oss.getBucket()))
                .key(key)
                .contentType(file.getContentType())
                .contentLength(file.getSize());

            ObjectCannedACL acl = getObjectCannedAcl(oss.getAccessPolicy());
            if (acl != null) {
                requestBuilder.acl(acl);
            }

            s3Client.putObject(requestBuilder.build(), RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            String url = buildUrl(oss, key);
            log.info("上传文件到对象存储成功，key={}，url={}", key, url);
            return url;
        } catch (IOException e) {
            log.error("读取上传文件流失败", e);
            throw new BusinessException("读取上传文件失败");
        } catch (S3Exception e) {
            log.error("上传文件到对象存储失败，status={}，errorCode={}，errorMessage={}",
                e.statusCode(), e.awsErrorDetails().errorCode(), e.awsErrorDetails().errorMessage(), e);
            throw new BusinessException("上传文件到对象存储失败：" + e.awsErrorDetails().errorMessage());
        } catch (SdkClientException e) {
            log.error("上传文件到对象存储客户端异常", e);
            throw new BusinessException("上传文件到对象存储失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("上传文件到对象存储失败", e);
            throw new BusinessException("上传文件到对象存储失败：" + e.getMessage());
        }
    }

    /**
     * 构建 S3 客户端
     */
    private S3Client buildClient(ObjectStorageProperties.Oss oss) {
        String endpoint = cleanBackticks(oss.getEndPoint());
        String region = resolveRegion(oss);
        boolean pathStyle = !isCloudService(endpoint);

        AwsBasicCredentials credentials = AwsBasicCredentials.create(
            cleanBackticks(oss.getAccessKeyId()),
            cleanBackticks(oss.getAccessKeySecret())
        );

        return S3Client.builder()
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .endpointOverride(URI.create(getEndpoint(endpoint, oss.getIsHttps())))
            .region(Region.of(region))
            .forcePathStyle(pathStyle)
            .build();
    }

    /**
     * 解析 region，优先使用配置值，否则从 endpoint 提取
     */
    private String resolveRegion(ObjectStorageProperties.Oss oss) {
        if (StrUtil.isNotBlank(oss.getRegion())) {
            return cleanBackticks(oss.getRegion());
        }
        String endpoint = cleanBackticks(oss.getEndPoint());
        // 阿里云：oss-cn-beijing.aliyuncs.com -> cn-beijing
        Matcher aliyunMatcher = Pattern.compile("oss-([a-z0-9-]+)\\.aliyuncs\\.com").matcher(endpoint);
        if (aliyunMatcher.find()) {
            return aliyunMatcher.group(1);
        }
        // 腾讯云：cos.ap-shanghai.myqcloud.com -> ap-shanghai
        Matcher cosMatcher = Pattern.compile("cos\\.([a-z0-9-]+)\\.myqcloud\\.com").matcher(endpoint);
        if (cosMatcher.find()) {
            return cosMatcher.group(1);
        }
        // 七牛云：s3-cn-north-1.qiniucs.com -> cn-north-1
        Matcher qiniuMatcher = Pattern.compile("s3-([a-z0-9-]+)\\.qiniucs\\.com").matcher(endpoint);
        if (qiniuMatcher.find()) {
            return qiniuMatcher.group(1);
        }
        // MinIO 等私有存储默认使用 us-east-1
        return "us-east-1";
    }

    /**
     * 是否为已知云服务商
     */
    private boolean isCloudService(String endpoint) {
        return CLOUD_SERVICES.stream().anyMatch(endpoint::contains);
    }

    /**
     * 根据 https 配置补全协议头
     */
    private String getEndpoint(String endpoint, String isHttps) {
        if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) {
            return endpoint;
        }
        return "Y".equalsIgnoreCase(isHttps) ? "https://" + endpoint : "http://" + endpoint;
    }

    /**
     * 获取对象 ACL
     */
    private ObjectCannedACL getObjectCannedAcl(String accessPolicy) {
        if (StrUtil.isBlank(accessPolicy)) {
            return ObjectCannedACL.PUBLIC_READ;
        }
        return switch (accessPolicy) {
            case "0" -> ObjectCannedACL.PRIVATE;
            case "1" -> ObjectCannedACL.PUBLIC_READ_WRITE;
            case "2" -> ObjectCannedACL.PUBLIC_READ;
            default -> ObjectCannedACL.PUBLIC_READ;
        };
    }

    /**
     * 去除 ``` 包裹
     */
    private String cleanBackticks(String value) {
        if (StrUtil.isBlank(value)) {
            return value;
        }
        String str = value.trim();
        if (str.startsWith("`") && str.endsWith("`")) {
            return str.substring(1, str.length() - 1).trim();
        }
        return str;
    }

    /**
     * 构建对象 key
     */
    private String buildKey(String prefix, String configPrefix, String dateDir, String fileName) {
        String fullPrefix = StrUtil.isNotBlank(prefix) ? prefix.trim().replaceAll("/$", "") : "";
        if (StrUtil.isNotBlank(configPrefix)) {
            String cfg = configPrefix.trim().replaceAll("/$", "");
            fullPrefix = StrUtil.isNotBlank(fullPrefix) ? cfg + "/" + fullPrefix : cfg;
        }
        return StrUtil.isNotBlank(fullPrefix)
            ? fullPrefix + "/" + dateDir + "/" + fileName
            : dateDir + "/" + fileName;
    }

    /**
     * 构建公网访问 URL
     */
    private String buildUrl(ObjectStorageProperties.Oss oss, String key) {
        String endpoint = cleanBackticks(oss.getEndPoint());
        String domain = cleanBackticks(oss.getDomain());
        String bucket = cleanBackticks(oss.getBucket());
        String protocol = "Y".equalsIgnoreCase(oss.getIsHttps()) ? "https://" : "http://";

        // 云服务商 virtual-hosted-style
        if (isCloudService(endpoint)) {
            if (StrUtil.isNotBlank(domain)) {
                return protocol + domain + "/" + key;
            }
            URI uri = URI.create(getEndpoint(endpoint, oss.getIsHttps()));
            String port = uri.getPort() == -1 ? "" : ":" + uri.getPort();
            return protocol + bucket + "." + uri.getHost() + port + "/" + key;
        }

        // MinIO 等 path-style
        if (StrUtil.isNotBlank(domain)) {
            return (domain.startsWith("http://") || domain.startsWith("https://"))
                ? domain + "/" + bucket + "/" + key
                : protocol + domain + "/" + bucket + "/" + key;
        }
        return getEndpoint(endpoint, oss.getIsHttps()) + "/" + bucket + "/" + key;
    }
}
