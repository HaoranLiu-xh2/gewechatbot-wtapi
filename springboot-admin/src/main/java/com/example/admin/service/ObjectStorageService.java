package com.example.admin.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 对象存储服务接口
 *
 * @author example
 */
public interface ObjectStorageService {

    /**
     * 上传文件到对象存储
     *
     * @param file 待上传文件
     * @return 可公网访问的文件 URL
     */
    String upload(MultipartFile file);

    /**
     * 上传文件到对象存储，指定目录前缀
     *
     * @param file   待上传文件
     * @param prefix 目录前缀，如 wx/image
     * @return 可公网访问的文件 URL
     */
    String upload(MultipartFile file, String prefix);
}
