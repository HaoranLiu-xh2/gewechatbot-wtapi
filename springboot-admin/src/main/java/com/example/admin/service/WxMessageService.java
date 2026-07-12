package com.example.admin.service;

import com.example.admin.dto.WxDownloadImageDTO;
import com.example.admin.dto.WxSendFileDTO;
import com.example.admin.dto.WxSendImageDTO;
import com.example.admin.dto.WxSendTextDTO;
import com.example.admin.dto.WxSendVideoDTO;
import com.example.admin.entity.WxMessage;

import java.util.List;
import java.util.Map;

/**
 * 微信消息业务接口
 *
 * @author example
 */
public interface WxMessageService {

    /**
     * 处理第三方消息回调
     *
     * @param payload 回调消息体
     */
    void handleCallback(Map<String, Object> payload);

    /**
     * 发送文本消息
     *
     * @param sendTextDTO 发送参数
     * @return 保存后的本地消息记录
     */
    WxMessage sendTextMessage(WxSendTextDTO sendTextDTO);

    /**
     * 发送图片消息
     *
     * @param sendImageDTO 发送参数
     * @return 保存后的本地消息记录
     */
    WxMessage sendImageMessage(WxSendImageDTO sendImageDTO);

    /**
     * 发送文件消息
     *
     * @param sendFileDTO 发送参数
     * @return 保存后的本地消息记录
     */
    WxMessage sendFileMessage(WxSendFileDTO sendFileDTO);

    /**
     * 发送视频消息
     *
     * @param sendVideoDTO 发送参数
     * @return 保存后的本地消息记录
     */
    WxMessage sendVideoMessage(WxSendVideoDTO sendVideoDTO);

    /**
     * 查询当前用户与某个联系人的历史消息
     *
     * @param appId      设备 ID
     * @param wxid       当前登录微信的 wxid
     * @param contactWxid 联系人 wxid
     * @return 消息列表
     */
    List<WxMessage> listMessages(String appId, String wxid, String contactWxid);

    /**
     * 下载图片消息
     *
     * @param downloadImageDTO 下载参数
     * @return 下载结果，可能包含 base64、url 等字段
     */
    Map<String, Object> downloadImage(WxDownloadImageDTO downloadImageDTO);
}
