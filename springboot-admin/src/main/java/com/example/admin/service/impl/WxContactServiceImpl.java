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
import com.example.admin.entity.WxContact;
import com.example.admin.mapper.WxContactMapper;
import com.example.admin.service.WxContactService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 微信联系人业务实现类
 *
 * @author example
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WxContactServiceImpl extends ServiceImpl<WxContactMapper, WxContact> implements WxContactService {

    private static final String WX_API_BASE = "https://wx.chuapi.com";

    private static final int BATCH_SIZE = 100;

    private static final int TYPE_FRIEND = 1;

    private static final int TYPE_CHATROOM = 2;

    private static final int TYPE_GH = 3;

    @Override
    public List<WxContact> listContacts(String ownerWxid) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        LambdaQueryWrapper<WxContact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WxContact::getUserId, userId)
                .eq(WxContact::getOwnerWxid, ownerWxid)
                .orderByDesc(WxContact::getLastMsgTime)
                .orderByDesc(WxContact::getCreateTime);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public void updateLastMessage(Long userId, String ownerWxid, String contactWxid, String lastMsgContent, Long lastMsgTime) {
        if (userId == null || StrUtil.isBlank(ownerWxid) || StrUtil.isBlank(contactWxid)) {
            return;
        }
        LambdaQueryWrapper<WxContact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WxContact::getUserId, userId)
                .eq(WxContact::getOwnerWxid, ownerWxid)
                .eq(WxContact::getContactWxid, contactWxid);
        WxContact contact = new WxContact();
        contact.setLastMsgContent(lastMsgContent);
        contact.setLastMsgTime(lastMsgTime);
        baseMapper.update(contact, wrapper);
    }

    @Override
    @Async("wxContactTaskExecutor")
    public void syncContacts(Long userId, String appId, String ownerWxid, String wxToken) {
        if (userId == null || StrUtil.isBlank(appId) || StrUtil.isBlank(ownerWxid) || StrUtil.isBlank(wxToken)) {
            log.warn("同步通讯录参数不完整，跳过同步：userId={}, appId={}, ownerWxid={}", userId, appId, ownerWxid);
            return;
        }

        log.info("开始同步微信通讯录：userId={}, appId={}, ownerWxid={}", userId, appId, ownerWxid);
        try {
            // 1. 获取通讯录列表
            Map<String, Object> contactsList = fetchContactsList(appId, wxToken);
            if (contactsList == null) {
                log.warn("获取通讯录列表为空：userId={}, appId={}, ownerWxid={}", userId, appId, ownerWxid);
                return;
            }

            @SuppressWarnings("unchecked")
            List<String> friends = contactsList.get("friends") instanceof List
                    ? (List<String>) contactsList.get("friends") : Collections.emptyList();
            @SuppressWarnings("unchecked")
            List<String> chatrooms = contactsList.get("chatrooms") instanceof List
                    ? (List<String>) contactsList.get("chatrooms") : Collections.emptyList();
            @SuppressWarnings("unchecked")
            List<String> ghs = contactsList.get("ghs") instanceof List
                    ? (List<String>) contactsList.get("ghs") : Collections.emptyList();

            log.info("通讯录列表获取成功：friends={}, chatrooms={}, ghs={}", friends.size(), chatrooms.size(), ghs.size());

            // 2. 先物理清除该账号旧的联系人数据，避免唯一索引冲突
            LambdaQueryWrapper<WxContact> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(WxContact::getUserId, userId)
                    .eq(WxContact::getOwnerWxid, ownerWxid);
            baseMapper.delete(deleteWrapper);

            // 3. 分批获取简要信息并保存
            saveBriefInfoBatch(userId, appId, ownerWxid, wxToken, friends, TYPE_FRIEND);
            saveBriefInfoBatch(userId, appId, ownerWxid, wxToken, chatrooms, TYPE_CHATROOM);
            saveBriefInfoBatch(userId, appId, ownerWxid, wxToken, ghs, TYPE_GH);

            log.info("微信通讯录同步完成：userId={}, appId={}, ownerWxid={}", userId, appId, ownerWxid);
        } catch (Exception e) {
            log.error("同步微信通讯录异常：userId={}, appId={}, ownerWxid={}", userId, appId, ownerWxid, e);
        }
    }

    /**
     * 获取通讯录列表
     */
    private Map<String, Object> fetchContactsList(String appId, String wxToken) {
        String url = WX_API_BASE + "/finder/v2/api/contacts/fetchContactsList";
        Map<String, String> headers = new HashMap<>(2);
        headers.put("X-finder-TOKEN", wxToken);

        Map<String, Object> body = new HashMap<>(2);
        body.put("appId", appId);

        String response = HttpClientUtil.postJson(url, body, headers);
        log.debug("获取通讯录列表响应：{}", response);

        Map<String, Object> result = parseResponse(response);
        Object dataObj = result.get("data");
        if (dataObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) dataObj;
            return data;
        }
        return null;
    }

    /**
     * 分批获取简要信息并保存
     */
    private void saveBriefInfoBatch(Long userId, String appId, String ownerWxid, String wxToken,
                                    List<String> wxids, Integer type) {
        if (wxids == null || wxids.isEmpty()) {
            return;
        }

        for (int i = 0; i < wxids.size(); i += BATCH_SIZE) {
            List<String> batch = wxids.subList(i, Math.min(i + BATCH_SIZE, wxids.size()));
            try {
                List<Map<String, Object>> briefInfos = fetchBriefInfo(appId, wxToken, batch);
                List<WxContact> contacts = new ArrayList<>(briefInfos.size());
                for (Map<String, Object> info : briefInfos) {
                    WxContact contact = convertToContact(userId, appId, ownerWxid, info, type);
                    contacts.add(contact);
                }
                if (!contacts.isEmpty()) {
                    this.saveBatch(contacts);
                }
            } catch (Exception e) {
                log.error("获取简要信息异常：userId={}, appId={}, ownerWxid={}, type={}, batchIndex={}",
                        userId, appId, ownerWxid, type, i / BATCH_SIZE, e);
            }
        }
    }

    /**
     * 获取联系人简要信息
     */
    private List<Map<String, Object>> fetchBriefInfo(String appId, String wxToken, List<String> wxids) {
        String url = WX_API_BASE + "/finder/v2/api/contacts/getBriefInfo";
        Map<String, String> headers = new HashMap<>(2);
        headers.put("X-finder-TOKEN", wxToken);

        Map<String, Object> body = new HashMap<>(4);
        body.put("appId", appId);
        body.put("wxids", wxids);

        String response = HttpClientUtil.postJson(url, body, headers);
        log.debug("获取联系人简要信息响应：{}", response);

        Map<String, Object> result = parseResponse(response);
        Object dataObj = result.get("data");
        if (dataObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> data = (List<Map<String, Object>>) dataObj;
            return data;
        }
        return Collections.emptyList();
    }

    /**
     * 将接口返回数据转换为联系人实体
     */
    private WxContact convertToContact(Long userId, String appId, String ownerWxid, Map<String, Object> info, Integer type) {
        WxContact contact = new WxContact();
        contact.setUserId(userId);
        contact.setOwnerWxid(ownerWxid);
        contact.setContactWxid(getStringValue(info, "userName"));
        contact.setUserName(getStringValue(info, "userName"));
        contact.setNickName(getStringValue(info, "nickName"));
        contact.setAlias(getStringValue(info, "alias"));
        contact.setRemark(getStringValue(info, "remark"));
        contact.setSex(getIntegerValue(info, "sex"));
        contact.setSignature(getStringValue(info, "signature"));
        contact.setCountry(getStringValue(info, "country"));
        contact.setProvince(getStringValue(info, "province"));
        contact.setCity(getStringValue(info, "city"));
        contact.setBigHeadImgUrl(getStringValue(info, "bigHeadImgUrl"));
        contact.setSmallHeadImgUrl(getStringValue(info, "smallHeadImgUrl"));
        contact.setPhoneNum(extractFirstPhone(info.get("phoneNumList")));
        contact.setType(type);
        contact.setRawData(JSON.toJSONString(info));
        return contact;
    }

    /**
     * 解析第三方接口响应
     */
    private Map<String, Object> parseResponse(String response) {
        if (StrUtil.isBlank(response)) {
            throw new RuntimeException("微信接口返回为空");
        }
        try {
            return JSON.parseObject(response, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("解析微信接口响应失败：{}", response, e);
            throw new RuntimeException("解析微信接口响应失败");
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
        String str = value.toString().trim();
        if (str.startsWith("`") && str.endsWith("`")) {
            str = str.substring(1, str.length() - 1).trim();
        }
        return str;
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
     * 从手机号列表中提取第一个手机号
     */
    private String extractFirstPhone(Object phoneListObj) {
        if (phoneListObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> phoneList = (List<Object>) phoneListObj;
            if (!phoneList.isEmpty() && phoneList.get(0) != null) {
                return phoneList.get(0).toString().trim();
            }
        }
        return null;
    }
}
