package com.example.admin.controller;

import com.example.admin.common.result.Result;
import com.example.admin.service.ObjectStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传控制器
 *
 * @author example
 */
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final ObjectStorageService objectStorageService;

    /**
     * 上传文件到对象存储
     *
     * @param file   待上传文件
     * @param prefix 目录前缀，默认 wx
     * @return 文件访问 URL
     */
    @PostMapping
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file,
                                              @RequestParam(value = "prefix", required = false, defaultValue = "wx") String prefix) {
        String url = objectStorageService.upload(file, prefix);
        Map<String, String> result = new HashMap<>(2);
        result.put("url", url);
        return Result.success(result);
    }
}
