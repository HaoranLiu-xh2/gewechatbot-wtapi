package com.example.admin.service;

import com.example.admin.entity.WxContact;

import java.util.List;

/**
 * 微信联系人业务接口
 *
 * @author example
 */
public interface WxContactService {

    /**
     * 异步同步当前微信账号的通讯录（好友、群聊、公众号）
     *
     * @param userId    系统用户 ID
     * @param appId     微信应用 ID
     * @param ownerWxid 当前登录微信的 wxid
     * @param wxToken   微信 API Token
     */
    void syncContacts(Long userId, String appId, String ownerWxid, String wxToken);

    /**
     * 查询当前登录用户的联系人列表
     *
     * @param ownerWxid 当前登录微信的 wxid
     * @return 联系人列表
     */
    List<WxContact> listContacts(String ownerWxid);

    /**
     * 更新联系人最后一条消息预览
     *
     * @param userId         系统用户 ID
     * @param ownerWxid      当前登录微信的 wxid
     * @param contactWxid    联系人 wxid
     * @param lastMsgContent 最后一条消息内容
     * @param lastMsgTime    最后一条消息时间戳
     */
    void updateLastMessage(Long userId, String ownerWxid, String contactWxid, String lastMsgContent, Long lastMsgTime);
}
