package com.example.admin.common.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Map;

/**
 * HTTP 请求工具类（基于 Hutool HttpUtil 封装）
 *
 * @author example
 */
@Slf4j
public class HttpClientUtil {

    /**
     * 默认连接超时时间（毫秒）
     */
    private static final int DEFAULT_TIMEOUT = 10000;

    /**
     * 发送 GET 请求
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param headers 请求头
     * @return 响应字符串
     */
    public static String get(String url, Map<String, Object> params, Map<String, String> headers) {
        if (params != null && !params.isEmpty()) {
            url = HttpUtil.urlWithForm(url, params, null, true);
        }
        HttpRequest request = HttpRequest.get(url).timeout(DEFAULT_TIMEOUT);
        addHeaders(request, headers);
        try (HttpResponse response = request.execute()) {
            return response.body();
        } catch (Exception e) {
            log.error("GET 请求异常，url：{}，参数：{}，异常：{}", url, params, e.getMessage(), e);
            throw new RuntimeException("GET 请求异常：" + e.getMessage());
        }
    }

    /**
     * 发送 GET 请求（无参数无请求头）
     */
    public static String get(String url) {
        return get(url, null, null);
    }

    /**
     * 发送 POST JSON 请求
     *
     * @param url     请求地址
     * @param body    请求体对象
     * @param headers 请求头
     * @return 响应字符串
     */
    public static String postJson(String url, Object body, Map<String, String> headers) {
        HttpRequest request = HttpRequest.post(url)
                .body(JSON.toJSONString(body))
                .header("Content-Type", "application/json")
                .timeout(DEFAULT_TIMEOUT);
        addHeaders(request, headers);
        try (HttpResponse response = request.execute()) {
            return response.body();
        } catch (Exception e) {
            log.error("POST JSON 请求异常，url：{}，body：{}，异常：{}", url, body, e.getMessage(), e);
            throw new RuntimeException("POST JSON 请求异常：" + e.getMessage());
        }
    }

    /**
     * 发送 POST JSON 请求
     */
    public static String postJson(String url, Object body) {
        return postJson(url, body, null);
    }

    /**
     * 发送 POST JSON 请求，返回响应字节数组
     *
     * @param url     请求地址
     * @param body    请求体对象
     * @param headers 请求头
     * @return 响应字节数组
     */
    public static byte[] postJsonForBytes(String url, Object body, Map<String, String> headers) {
        HttpRequest request = HttpRequest.post(url)
                .body(JSON.toJSONString(body))
                .header("Content-Type", "application/json")
                .timeout(DEFAULT_TIMEOUT);
        addHeaders(request, headers);
        try (HttpResponse response = request.execute()) {
            return response.bodyBytes();
        } catch (Exception e) {
            log.error("POST JSON 请求异常，url：{}，body：{}，异常：{}", url, body, e.getMessage(), e);
            throw new RuntimeException("POST JSON 请求异常：" + e.getMessage());
        }
    }

    /**
     * 发送 POST Form 请求
     *
     * @param url     请求地址
     * @param params  表单参数
     * @param headers 请求头
     * @return 响应字符串
     */
    public static String postForm(String url, Map<String, Object> params, Map<String, String> headers) {
        HttpRequest request = HttpRequest.post(url)
                .form(params)
                .timeout(DEFAULT_TIMEOUT);
        addHeaders(request, headers);
        try (HttpResponse response = request.execute()) {
            return response.body();
        } catch (Exception e) {
            log.error("POST Form 请求异常，url：{}，参数：{}，异常：{}", url, params, e.getMessage(), e);
            throw new RuntimeException("POST Form 请求异常：" + e.getMessage());
        }
    }

    /**
     * 发送 PUT JSON 请求
     */
    public static String putJson(String url, Object body, Map<String, String> headers) {
        HttpRequest request = HttpRequest.put(url)
                .body(JSON.toJSONString(body))
                .header("Content-Type", "application/json")
                .timeout(DEFAULT_TIMEOUT);
        addHeaders(request, headers);
        try (HttpResponse response = request.execute()) {
            return response.body();
        } catch (Exception e) {
            log.error("PUT JSON 请求异常，url：{}，body：{}，异常：{}", url, body, e.getMessage(), e);
            throw new RuntimeException("PUT JSON 请求异常：" + e.getMessage());
        }
    }

    /**
     * 发送 DELETE 请求
     */
    public static String delete(String url, Map<String, String> headers) {
        HttpRequest request = HttpRequest.delete(url).timeout(DEFAULT_TIMEOUT);
        addHeaders(request, headers);
        try (HttpResponse response = request.execute()) {
            return response.body();
        } catch (Exception e) {
            log.error("DELETE 请求异常，url：{}，异常：{}", url, e.getMessage(), e);
            throw new RuntimeException("DELETE 请求异常：" + e.getMessage());
        }
    }

    /**
     * 上传文件
     *
     * @param url     请求地址
     * @param file    待上传文件
     * @param params  附加表单参数
     * @param headers 请求头
     * @return 响应字符串
     */
    public static String upload(String url, File file, Map<String, Object> params, Map<String, String> headers) {
        HttpRequest request = HttpRequest.post(url).timeout(DEFAULT_TIMEOUT);
        if (file != null && file.exists()) {
            request.form("file", file);
        }
        if (params != null && !params.isEmpty()) {
            params.forEach(request::form);
        }
        addHeaders(request, headers);
        try (HttpResponse response = request.execute()) {
            return response.body();
        } catch (Exception e) {
            log.error("文件上传异常，url：{}，异常：{}", url, e.getMessage(), e);
            throw new RuntimeException("文件上传异常：" + e.getMessage());
        }
    }

    /**
     * 添加请求头
     */
    private static void addHeaders(HttpRequest request, Map<String, String> headers) {
        if (headers != null && !headers.isEmpty()) {
            headers.forEach((key, value) -> {
                if (StrUtil.isNotBlank(key)) {
                    request.header(key, value, true);
                }
            });
        }
    }
}
