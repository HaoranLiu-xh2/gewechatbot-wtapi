package com.example.admin.controller;

import com.example.admin.common.result.Result;
import com.example.admin.dto.WxDownloadImageDTO;
import com.example.admin.dto.WxSendFileDTO;
import com.example.admin.dto.WxSendImageDTO;
import com.example.admin.dto.WxSendTextDTO;
import com.example.admin.dto.WxSendVideoDTO;
import com.example.admin.entity.WxMessage;
import com.example.admin.service.WxMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 微信消息控制器
 *
 * @author example
 */
@RestController
@RequestMapping("/api/wx/message")
@RequiredArgsConstructor
public class WxMessageController {

    private final WxMessageService wxMessageService;

    /**
     * 接收第三方消息回调（公开接口，供 wx.chuapi.com 推送）
     *
     * @param payload 回调消息体
     * @return 接收结果
     */
    @PostMapping("/callback")
    public Result<Void> receiveCallback(@RequestBody Map<String, Object> payload) {
        wxMessageService.handleCallback(payload);
        return Result.success("已接收");
    }

    /**
     * 发送文本消息
     *
     * @param sendTextDTO 发送参数
     * @return 保存后的本地消息记录
     */
    @PostMapping("/send-text")
    public Result<WxMessage> sendText(@Valid @RequestBody WxSendTextDTO sendTextDTO) {
        WxMessage message = wxMessageService.sendTextMessage(sendTextDTO);
        return Result.success(message);
    }

    /**
     * 发送图片消息
     *
     * @param sendImageDTO 发送参数
     * @return 保存后的本地消息记录
     */
    @PostMapping("/send-image")
    public Result<WxMessage> sendImage(@Valid @RequestBody WxSendImageDTO sendImageDTO) {
        WxMessage message = wxMessageService.sendImageMessage(sendImageDTO);
        return Result.success(message);
    }

    /**
     * 发送文件消息
     *
     * @param sendFileDTO 发送参数
     * @return 保存后的本地消息记录
     */
    @PostMapping("/send-file")
    public Result<WxMessage> sendFile(@Valid @RequestBody WxSendFileDTO sendFileDTO) {
        WxMessage message = wxMessageService.sendFileMessage(sendFileDTO);
        return Result.success(message);
    }

    /**
     * 发送视频消息
     *
     * @param sendVideoDTO 发送参数
     * @return 保存后的本地消息记录
     */
    @PostMapping("/send-video")
    public Result<WxMessage> sendVideo(@Valid @RequestBody WxSendVideoDTO sendVideoDTO) {
        WxMessage message = wxMessageService.sendVideoMessage(sendVideoDTO);
        return Result.success(message);
    }

    /**
     * 查询历史消息
     *
     * @param appId       设备 ID
     * @param wxid        当前登录微信 wxid
     * @param contactWxid 联系人 wxid
     * @return 消息列表
     */
    @GetMapping("/list")
    public Result<List<WxMessage>> listMessages(@RequestParam String appId,
                                                @RequestParam String wxid,
                                                @RequestParam String contactWxid) {
        List<WxMessage> list = wxMessageService.listMessages(appId, wxid, contactWxid);
        return Result.success(list);
    }

    /**
     * 下载图片消息
     *
     * @param downloadImageDTO 下载参数
     * @return 下载结果
     */
    @PostMapping("/download-image")
    public Result<Map<String, Object>> downloadImage(@Valid @RequestBody WxDownloadImageDTO downloadImageDTO) {
        Map<String, Object> result = wxMessageService.downloadImage(downloadImageDTO);
        return Result.success(result);
    }
}
