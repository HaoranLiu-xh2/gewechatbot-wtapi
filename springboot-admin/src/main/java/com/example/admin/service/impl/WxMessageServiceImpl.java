package com.example.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.admin.common.exception.BusinessException;
import com.example.admin.common.result.ResultCode;
import com.example.admin.common.utils.HttpClientUtil;
import com.example.admin.common.utils.UserContext;
import com.example.admin.dto.WxDownloadImageDTO;
import com.example.admin.dto.WxSendFileDTO;
import com.example.admin.dto.WxSendImageDTO;
import com.example.admin.dto.WxSendTextDTO;
import com.example.admin.dto.WxSendVideoDTO;
import com.example.admin.entity.User;
import com.example.admin.entity.WxAccount;
import com.example.admin.entity.WxMessage;
import com.example.admin.mapper.UserMapper;
import com.example.admin.mapper.WxAccountMapper;
import com.example.admin.mapper.WxMessageMapper;
import com.example.admin.service.WxContactService;
import com.example.admin.service.WxMessageService;
import com.example.admin.websocket.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 微信消息业务实现类
 *
 * @author example
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WxMessageServiceImpl extends ServiceImpl<WxMessageMapper, WxMessage> implements WxMessageService {

    private static final String WX_API_BASE = "https://wx.chuapi.com";

    private static final String MSG_TYPE_ADD_MSG = "AddMsg";

    private static final int MSG_TYPE_TEXT = 1;

    private static final int MSG_TYPE_IMAGE = 3;

    private static final int MSG_TYPE_FILE = 6;

    private static final int MSG_TYPE_VIDEO = 43;

    private static final int IMAGE_DOWNLOAD_THUMB = 3;

    private final UserMapper userMapper;

    private final WxAccountMapper wxAccountMapper;

    private final WxContactService wxContactService;

    @Override
    public void handleCallback(Map<String, Object> payload) {
        if (payload == null) {
            return;
        }
        String typeName = getStringValue(payload, "TypeName");
        if (!MSG_TYPE_ADD_MSG.equals(typeName)) {
            log.debug("非 AddMsg 消息类型，跳过：{}", typeName);
            return;
        }

        String appId = getStringValue(payload, "Appid");
        String wxid = getStringValue(payload, "Wxid");
        if (StrUtil.isBlank(appId) || StrUtil.isBlank(wxid)) {
            log.warn("回调消息缺少 Appid 或 Wxid：{}", payload);
            return;
        }

        // 根据 appId 找到所属用户
        LambdaQueryWrapper<WxAccount> accountWrapper = new LambdaQueryWrapper<>();
        accountWrapper.eq(WxAccount::getAppId, appId)
                .eq(WxAccount::getDeleted, 0)
                .last("LIMIT 1");
        WxAccount account = wxAccountMapper.selectOne(accountWrapper);
        if (account == null) {
            log.warn("未找到 Appid 对应的微信账号：{}" , appId);
            return;
        }
        Long userId = account.getUserId();

        Object dataObj = payload.get("Data");
        if (!(dataObj instanceof Map)) {
            log.warn("回调消息 Data 字段格式错误：{}", payload);
            return;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) dataObj;

        String fromWxid = extractStringString(data, "FromUserName");
        String toWxid = extractStringString(data, "ToUserName");
        String content = extractStringString(data, "Content");
        Integer msgType = getIntegerValue(data, "MsgType");
        Long msgId = getLongValue(data, "MsgId");
        Long newMsgId = getLongValue(data, "NewMsgId");
        Long createTime = getLongValue(data, "CreateTime");

        // 目前只处理文本和图片消息
        if (!Integer.valueOf(MSG_TYPE_TEXT).equals(msgType) && !Integer.valueOf(MSG_TYPE_IMAGE).equals(msgType)) {
            log.debug("暂不处理的消息类型，跳过：msgType={}", msgType);
            return;
        }

        String displayContent = content;
        String lastMsgContent = content;
        if (Integer.valueOf(MSG_TYPE_IMAGE).equals(msgType)) {
            lastMsgContent = "[图片]";
        }

        WxMessage message = new WxMessage();
        message.setUserId(userId);
        message.setAppId(appId);
        message.setWxid(wxid);
        message.setMsgId(msgId);
        message.setNewMsgId(newMsgId);
        message.setFromWxid(fromWxid);
        message.setToWxid(toWxid);
        message.setMsgType(msgType);
        message.setContent(displayContent);
        message.setMsgTime(createTime);
        message.setRawData(JSON.toJSONString(payload));
        baseMapper.insert(message);

        // 更新联系人最后消息预览
        String contactWxid = Objects.equals(toWxid, wxid) ? fromWxid : toWxid;
        if (StrUtil.isNotBlank(contactWxid) && !Objects.equals(contactWxid, wxid)) {
            wxContactService.updateLastMessage(userId, wxid, contactWxid, lastMsgContent, createTime);
        }

        // 把本地消息 ID 回写到 Data，方便前端后续操作（如下载原图），使用字符串避免前端精度丢失
        data.put("LocalMessageId", String.valueOf(message.getId()));

        // 图片消息下载缩略图后推送简化结构，避免把完整回调数据直接返给前端
        Map<String, Object> pushPayload;
        if (Integer.valueOf(MSG_TYPE_IMAGE).equals(msgType)) {
            pushPayload = handleImageCallback(message, appId, wxid, data, content);
        } else {
            pushPayload = payload;
        }

        ChatWebSocketHandler.sendMessage(userId, pushPayload);
        log.info("收到并推送消息：userId={}, appId={}, from={}, to={}, msgType={}, localMsgId={}", userId, appId, fromWxid, toWxid, msgType, message.getId());
    }

    /**
     * 处理图片回调：下载缩略图、更新消息 content 为 JSON（xml+thumb）、返回简化推送体
     */
    private Map<String, Object> handleImageCallback(WxMessage message, String appId, String wxid,
                                                    Map<String, Object> data, String xml) {
        Map<String, Object> thumbResult = downloadImageFromApi(appId, xml, IMAGE_DOWNLOAD_THUMB);
        String thumbSrc = extractImageSrc(thumbResult);

        // 将 xml 和缩略图地址/base64 一起存到 content
        Map<String, Object> imageContent = new HashMap<>(4);
        imageContent.put("xml", xml);
        if (StrUtil.isNotBlank(thumbSrc)) {
            imageContent.put("thumb", thumbSrc);
        }
        String contentJson = JSON.toJSONString(imageContent);
        message.setContent(contentJson);
        baseMapper.updateById(message);

        if (StrUtil.isNotBlank(thumbSrc)) {
            return buildImagePushPayload(appId, wxid, data, message.getId(), thumbSrc, contentJson);
        }

        // 缩略图下载失败，回退到原始回调（前端仍可尝试使用 ImgBuf.buffer）
        data.put("LocalMessageId", message.getId());
        return data;
    }

    /**
     * 调用第三方下载图片接口
     */
    private Map<String, Object> downloadImageFromApi(String appId, String xml, int type) {
        try {
            WxAccount account = getAccountByAppId(appId);
            if (account == null) {
                log.warn("下载图片时未找到对应账号：appId={}", appId);
                return Collections.emptyMap();
            }
            Long userId = account.getUserId();
            User user = userMapper.selectById(userId);
            if (user == null || StrUtil.isBlank(user.getToken())) {
                log.warn("下载图片时用户未设置 Token：userId={}", userId);
                return Collections.emptyMap();
            }

            String url = WX_API_BASE + "/finder/v2/api/message/downloadImage";
            Map<String, String> headers = new HashMap<>(2);
            headers.put("X-finder-TOKEN", user.getToken());

            Map<String, Object> body = new HashMap<>(4);
            body.put("appId", appId);
            body.put("type", type);
            body.put("xml", xml);

            byte[] responseBytes = HttpClientUtil.postJsonForBytes(url, body, headers);
            String responseStr = new String(responseBytes, java.nio.charset.StandardCharsets.UTF_8);

            if (StrUtil.isNotBlank(responseStr) && (responseStr.trim().startsWith("{") || responseStr.trim().startsWith("["))) {
                return JSON.parseObject(responseStr, new TypeReference<Map<String, Object>>() {});
            }

            // 二进制数据
            String base64 = java.util.Base64.getEncoder().encodeToString(responseBytes);
            Map<String, Object> result = new HashMap<>(2);
            result.put("base64", "data:image/jpeg;base64," + base64);
            return result;
        } catch (Exception e) {
            log.error("下载图片异常：appId={}，type={}", appId, type, e);
            return Collections.emptyMap();
        }
    }

    /**
     * 从下载结果中提取可展示的图片地址或 base64
     */
    private String extractImageSrc(Map<String, Object> result) {
        if (result == null || result.isEmpty()) {
            return null;
        }
        Object dataObj = result.get("data");
        if (!(dataObj instanceof Map)) {
            Object base64 = result.get("base64");
            if (base64 != null) {
                return cleanBackticks(base64.toString());
            }
            Object url = result.get("url");
            if (url != null) {
                return cleanBackticks(url.toString());
            }
            return null;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) dataObj;
        Object innerDataObj = data.get("data");
        @SuppressWarnings("unchecked")
        Map<String, Object> target = innerDataObj instanceof Map
                ? (Map<String, Object>) innerDataObj : data;

        String base64 = getStringValue(target, "base64");
        if (StrUtil.isNotBlank(base64)) {
            base64 = cleanBackticks(base64);
            return base64.startsWith("data:") ? base64 : "data:image/jpeg;base64," + base64;
        }

        String url = getStringValue(target, "url");
        if (StrUtil.isBlank(url)) {
            url = getStringValue(target, "fileUrl");
        }
        if (StrUtil.isNotBlank(url)) {
            return cleanBackticks(url);
        }
        return null;
    }

    /**
     * 构造图片消息的简化推送体，避免把完整回调数据返回给前端
     */
    private Map<String, Object> buildImagePushPayload(String appId, String wxid, Map<String, Object> data,
                                                      Long localMessageId, String thumbSrc, String contentJson) {
        Map<String, Object> pushPayload = new HashMap<>(4);
        pushPayload.put("TypeName", "AddMsg");
        pushPayload.put("Appid", appId);
        pushPayload.put("Wxid", wxid);

        Map<String, Object> pushData = new HashMap<>(8);
        pushData.put("MsgId", data.get("MsgId"));
        pushData.put("NewMsgId", data.get("NewMsgId"));
        pushData.put("FromUserName", data.get("FromUserName"));
        pushData.put("ToUserName", data.get("ToUserName"));
        pushData.put("MsgType", MSG_TYPE_IMAGE);
        pushData.put("Content", Collections.singletonMap("string", thumbSrc));
        pushData.put("CreateTime", data.get("CreateTime"));
        pushData.put("LocalMessageId", String.valueOf(localMessageId));
        pushData.put("ImageContent", contentJson);
        pushPayload.put("Data", pushData);
        return pushPayload;
    }

    /**
     * 从图片消息的 content 中提取 XML。
     * 新格式 content 为 JSON：{"xml":"...","thumb":"..."}；旧格式直接是 XML。
     */
    private String extractImageXml(String content) {
        if (StrUtil.isBlank(content)) {
            return null;
        }
        String trimmed = content.trim();
        if (trimmed.startsWith("<?xml") || trimmed.startsWith("<msg")) {
            return content;
        }
        try {
            Map<String, Object> map = JSON.parseObject(trimmed, new TypeReference<Map<String, Object>>() {});
            Object xml = map.get("xml");
            return xml != null ? xml.toString() : null;
        } catch (Exception e) {
            log.warn("解析图片消息 content 失败：{}", content);
            return null;
        }
    }

    /**
     * 根据 appId 获取账号（不校验当前登录用户，用于回调场景）
     */
    private WxAccount getAccountByAppId(String appId) {
        LambdaQueryWrapper<WxAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WxAccount::getAppId, appId)
                .eq(WxAccount::getDeleted, 0)
                .last("LIMIT 1");
        return wxAccountMapper.selectOne(wrapper);
    }

    @Override
    public WxMessage sendTextMessage(WxSendTextDTO sendTextDTO) {
        Long userId = UserContext.getUserId();
        WxAccount account = validateAccount(userId, sendTextDTO.getAppId());

        Map<String, Object> body = new HashMap<>(4);
        body.put("appId", sendTextDTO.getAppId());
        body.put("toWxid", sendTextDTO.getToWxid());
        body.put("content", sendTextDTO.getContent());

        return doSendMessage("/finder/v2/api/message/postText", body, account,
                sendTextDTO.getToWxid(), MSG_TYPE_TEXT, sendTextDTO.getContent());
    }

    @Override
    public WxMessage sendImageMessage(WxSendImageDTO sendImageDTO) {
        Long userId = UserContext.getUserId();
        WxAccount account = validateAccount(userId, sendImageDTO.getAppId());

        String imgUrl = cleanBackticks(sendImageDTO.getImgUrl());
        Map<String, Object> body = new HashMap<>(4);
        body.put("appId", sendImageDTO.getAppId());
        body.put("toWxid", sendImageDTO.getToWxid());
        body.put("imgUrl", imgUrl);

        return doSendMessage("/finder/v2/api/message/postImage", body, account,
                sendImageDTO.getToWxid(), MSG_TYPE_IMAGE, imgUrl, "[图片]");
    }

    @Override
    public WxMessage sendFileMessage(WxSendFileDTO sendFileDTO) {
        Long userId = UserContext.getUserId();
        WxAccount account = validateAccount(userId, sendFileDTO.getAppId());

        String fileUrl = cleanBackticks(sendFileDTO.getFileUrl());
        Map<String, Object> body = new HashMap<>(4);
        body.put("appId", sendFileDTO.getAppId());
        body.put("toWxid", sendFileDTO.getToWxid());
        body.put("fileName", sendFileDTO.getFileName());
        body.put("fileUrl", fileUrl);

        return doSendMessage("/finder/v2/api/message/postFile", body, account,
                sendFileDTO.getToWxid(), MSG_TYPE_FILE, fileUrl, sendFileDTO.getFileName());
    }

    @Override
    public WxMessage sendVideoMessage(WxSendVideoDTO sendVideoDTO) {
        Long userId = UserContext.getUserId();
        WxAccount account = validateAccount(userId, sendVideoDTO.getAppId());

        String videoUrl = cleanBackticks(sendVideoDTO.getVideoUrl());
        Map<String, Object> body = new HashMap<>(4);
        body.put("appId", sendVideoDTO.getAppId());
        body.put("toWxid", sendVideoDTO.getToWxid());
        body.put("videoUrl", videoUrl);
        if (StrUtil.isNotBlank(sendVideoDTO.getThumbUrl())) {
            body.put("thumbUrl", cleanBackticks(sendVideoDTO.getThumbUrl()));
        }
        if (sendVideoDTO.getVideoDuration() != null) {
            body.put("videoDuration", sendVideoDTO.getVideoDuration());
        }

        return doSendMessage("/finder/v2/api/message/postVideo", body, account,
                sendVideoDTO.getToWxid(), MSG_TYPE_VIDEO, videoUrl, "[视频]");
    }

    @Override
    public List<WxMessage> listMessages(String appId, String wxid, String contactWxid) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        LambdaQueryWrapper<WxMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WxMessage::getUserId, userId)
                .eq(WxMessage::getAppId, appId)
                .eq(WxMessage::getWxid, wxid)
                .and(w -> w
                        .eq(WxMessage::getFromWxid, contactWxid).eq(WxMessage::getToWxid, wxid)
                        .or()
                        .eq(WxMessage::getFromWxid, wxid).eq(WxMessage::getToWxid, contactWxid))
                .orderByAsc(WxMessage::getMsgTime);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public Map<String, Object> downloadImage(WxDownloadImageDTO downloadImageDTO) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        WxMessage message = baseMapper.selectById(downloadImageDTO.getMessageId());
        if (message == null || !userId.equals(message.getUserId())) {
            throw new BusinessException("消息不存在或无权访问");
        }
        if (!Integer.valueOf(MSG_TYPE_IMAGE).equals(message.getMsgType())) {
            throw new BusinessException("非图片消息");
        }
        String xml = extractImageXml(message.getContent());
        if (StrUtil.isBlank(xml)) {
            throw new BusinessException("图片消息缺少 XML 内容");
        }

        String wxToken = getCurrentWxToken();
        String url = WX_API_BASE + "/finder/v2/api/message/downloadImage";
        Map<String, String> headers = new HashMap<>(2);
        headers.put("X-finder-TOKEN", wxToken);

        Map<String, Object> body = new HashMap<>(4);
        body.put("appId", message.getAppId());
        body.put("type", downloadImageDTO.getType());
        body.put("xml", xml);

        byte[] responseBytes = HttpClientUtil.postJsonForBytes(url, body, headers);
        String responseStr = new String(responseBytes, java.nio.charset.StandardCharsets.UTF_8);

        // 优先尝试按 JSON 解析
        if (StrUtil.isNotBlank(responseStr) && (responseStr.trim().startsWith("{") || responseStr.trim().startsWith("["))) {
            try {
                Map<String, Object> result = JSON.parseObject(responseStr, new TypeReference<Map<String, Object>>() {});
                log.debug("下载图片返回 JSON：{}", responseStr);
                return extractImageDownloadResult(result);
            } catch (Exception e) {
                log.warn("下载图片响应解析 JSON 失败，按二进制处理：{}", e.getMessage());
            }
        }

        // 二进制数据转换为 base64
        String base64 = java.util.Base64.getEncoder().encodeToString(responseBytes);
        Map<String, Object> result = new HashMap<>(2);
        result.put("base64", "data:image/jpeg;base64," + base64);
        return result;
    }

    /**
     * 从第三方下载图片的 JSON 响应中提取可用字段
     */
    private Map<String, Object> extractImageDownloadResult(Map<String, Object> response) {
        Map<String, Object> result = new HashMap<>(4);
        Object dataObj = response.get("data");
        if (!(dataObj instanceof Map)) {
            result.put("raw", response);
            return result;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) dataObj;
        Object innerDataObj = data.get("data");
        @SuppressWarnings("unchecked")
        Map<String, Object> target = innerDataObj instanceof Map
                ? (Map<String, Object>) innerDataObj : data;

        String base64 = getStringValue(target, "base64");
        if (StrUtil.isNotBlank(base64)) {
            base64 = cleanBackticks(base64);
            if (!base64.startsWith("data:")) {
                base64 = "data:image/jpeg;base64," + base64;
            }
            result.put("base64", base64);
        }

        String url = getStringValue(target, "url");
        if (StrUtil.isBlank(url)) {
            url = getStringValue(target, "fileUrl");
        }
        if (StrUtil.isNotBlank(url)) {
            result.put("url", cleanBackticks(url));
        }

        if (result.isEmpty()) {
            result.put("raw", response);
        }
        return result;
    }

    /**
     * 校验 appId 是否属于当前用户，并返回对应账号
     */
    private WxAccount validateAccount(Long userId, String appId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        LambdaQueryWrapper<WxAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WxAccount::getUserId, userId)
                .eq(WxAccount::getAppId, appId)
                .eq(WxAccount::getDeleted, 0)
                .last("LIMIT 1");
        WxAccount account = wxAccountMapper.selectOne(wrapper);
        if (account == null) {
            throw new BusinessException("账号不存在或无权操作");
        }
        return account;
    }

    /**
     * 调用第三方消息发送接口并保存本地记录
     */
    private WxMessage doSendMessage(String apiPath, Map<String, Object> body, WxAccount account,
                                    String toWxid, Integer msgType, String content) {
        return doSendMessage(apiPath, body, account, toWxid, msgType, content, content);
    }

    /**
     * 调用第三方消息发送接口并保存本地记录
     *
     * @param lastMsgContent 用于联系人列表最后消息预览的文案
     */
    private WxMessage doSendMessage(String apiPath, Map<String, Object> body, WxAccount account,
                                    String toWxid, Integer msgType, String content, String lastMsgContent) {
        String wxToken = getCurrentWxToken();
        String url = WX_API_BASE + apiPath;
        Map<String, String> headers = new HashMap<>(2);
        headers.put("X-finder-TOKEN", wxToken);

        String response = HttpClientUtil.postJson(url, body, headers);
        log.debug("发送消息响应：apiPath={}，response={}", apiPath, response);
        Map<String, Object> result = parseResponse(response);

        long nowSeconds = System.currentTimeMillis() / 1000;
        WxMessage message = new WxMessage();
        message.setUserId(account.getUserId());
        message.setAppId(account.getAppId());
        message.setWxid(account.getWxid());
        message.setFromWxid(account.getWxid());
        message.setToWxid(toWxid);
        message.setMsgType(msgType);
        message.setContent(content);
        message.setMsgTime(nowSeconds);
        message.setRawData(JSON.toJSONString(result));
        baseMapper.insert(message);

        wxContactService.updateLastMessage(account.getUserId(), account.getWxid(), toWxid, lastMsgContent, nowSeconds);
        ChatWebSocketHandler.sendMessage(account.getUserId(), buildSendMessagePayload(message, account));

        return message;
    }

    /**
     * 去除第三方接口常见的 ``` 包裹
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
     * 构造发送消息推送体，与第三方回调格式保持一致
     */
    private Map<String, Object> buildSendMessagePayload(WxMessage message, WxAccount account) {
        Map<String, Object> payload = new HashMap<>(4);
        payload.put("TypeName", "AddMsg");
        payload.put("Appid", message.getAppId());
        payload.put("Wxid", account.getWxid());

        Map<String, Object> data = new HashMap<>(8);
        data.put("MsgId", message.getId());
        data.put("NewMsgId", message.getId());
        data.put("FromUserName", Collections.singletonMap("string", message.getFromWxid()));
        data.put("ToUserName", Collections.singletonMap("string", message.getToWxid()));
        data.put("MsgType", message.getMsgType());
        data.put("Content", Collections.singletonMap("string", message.getContent()));
        data.put("CreateTime", message.getMsgTime());
        payload.put("Data", data);
        return payload;
    }

    /**
     * 获取当前登录用户的微信 API Token
     */
    private String getCurrentWxToken() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (StrUtil.isBlank(user.getToken())) {
            throw new BusinessException("请先设置微信 API Token");
        }
        return user.getToken();
    }

    /**
     * 解析第三方接口响应
     */
    private Map<String, Object> parseResponse(String response) {
        if (StrUtil.isBlank(response)) {
            throw new BusinessException("微信接口返回为空");
        }
        try {
            return JSON.parseObject(response, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("解析微信接口响应失败：{}", response, e);
            throw new BusinessException("解析微信接口响应失败");
        }
    }

    /**
     * 从 Map 中获取字符串值
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        return value.toString().trim();
    }

    /**
     * 从 { string: value } 结构中提取字符串
     */
    private String extractStringString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> inner = (Map<String, Object>) value;
            Object stringValue = inner.get("string");
            if (stringValue != null) {
                return stringValue.toString().trim();
            }
        }
        return null;
    }

    /**
     * 从 Map 中获取整数值
     */
    private Integer getIntegerValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 从 Map 中获取长整数值
     */
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
