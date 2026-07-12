package com.example.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.admin.common.exception.BusinessException;
import com.example.admin.common.result.ResultCode;
import com.example.admin.common.utils.HttpClientUtil;
import com.example.admin.common.utils.UserContext;
import com.example.admin.dto.WxCheckLoginDTO;
import com.example.admin.dto.WxLogoutDTO;
import com.example.admin.dto.WxQrCodeDTO;
import com.example.admin.entity.User;
import com.example.admin.entity.WxAccount;
import com.example.admin.entity.WxContact;
import com.example.admin.mapper.UserMapper;
import com.example.admin.mapper.WxAccountMapper;
import com.example.admin.mapper.WxContactMapper;
import com.example.admin.service.WxAccountService;
import com.example.admin.service.WxContactService;
import com.example.admin.vo.WxAccountVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 微信账号业务实现类
 *
 * @author example
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WxAccountServiceImpl extends ServiceImpl<WxAccountMapper, WxAccount> implements WxAccountService {

    private static final String WX_API_BASE = "https://wx.chuapi.com";

    private static final Map<String, String> REGION_MAP = new LinkedHashMap<>();

    static {
        REGION_MAP.put("110000", "北京市");
        REGION_MAP.put("120000", "天津市");
        REGION_MAP.put("130000", "河北省");
        REGION_MAP.put("140000", "山西省");
        REGION_MAP.put("210000", "辽宁省");
        REGION_MAP.put("220000", "吉林省");
        REGION_MAP.put("230000", "黑龙江省");
        REGION_MAP.put("310000", "上海市");
        REGION_MAP.put("320000", "江苏省");
        REGION_MAP.put("330000", "浙江省");
        REGION_MAP.put("340000", "安徽省");
        REGION_MAP.put("350000", "福建省");
        REGION_MAP.put("360000", "江西省");
        REGION_MAP.put("370000", "山东省");
        REGION_MAP.put("410000", "河南省");
        REGION_MAP.put("420000", "湖北省");
        REGION_MAP.put("430000", "湖南省");
        REGION_MAP.put("440000", "广东省");
        REGION_MAP.put("450000", "广西省");
        REGION_MAP.put("460000", "海南省");
        REGION_MAP.put("500000", "重庆市");
        REGION_MAP.put("510000", "四川省");
        REGION_MAP.put("520000", "贵州省");
        REGION_MAP.put("530000", "云南省");
        REGION_MAP.put("540000", "西藏");
        REGION_MAP.put("610000", "陕西省");
        REGION_MAP.put("620000", "甘肃省");
        REGION_MAP.put("630000", "青海省");
        REGION_MAP.put("640000", "宁夏");
    }

    private final UserMapper userMapper;

    private final WxContactService wxContactService;

    private final WxContactMapper wxContactMapper;

    @Override
    public Map<String, Object> getLoginQrCode(WxQrCodeDTO qrCodeDTO) {
        String wxToken = getCurrentWxToken();
        String url = WX_API_BASE + "/finder/v2/api/login/getLoginQrCode";
        Map<String, String> headers = new HashMap<>(2);
        headers.put("X-finder-TOKEN", wxToken);

        Map<String, Object> body = new HashMap<>(8);
        body.put("appId", StrUtil.blankToDefault(qrCodeDTO.getAppId(), ""));
        body.put("aid", StrUtil.blankToDefault(qrCodeDTO.getAid(), ""));
        body.put("proxyIp", StrUtil.blankToDefault(qrCodeDTO.getProxyIp(), ""));
        body.put("regionId", qrCodeDTO.getRegionId());
        body.put("type", StrUtil.blankToDefault(qrCodeDTO.getType(), "mac"));

        String response = HttpClientUtil.postJson(url, body, headers);
        log.debug("获取微信登录二维码响应：{}", response);
        return parseResponse(response);
    }

    @Override
    public Map<String, Object> checkLogin(WxCheckLoginDTO checkLoginDTO) {
        String wxToken = getCurrentWxToken();
        String url = WX_API_BASE + "/finder/v2/api/login/checkLogin";
        Map<String, String> headers = new HashMap<>(2);
        headers.put("X-finder-TOKEN", wxToken);

        Map<String, Object> body = new HashMap<>(4);
        body.put("appId", checkLoginDTO.getAppId());
        body.put("uuid", checkLoginDTO.getUuid());
        body.put("autoSliding", checkLoginDTO.getAutoSliding() != null ? checkLoginDTO.getAutoSliding() : true);

        String response = HttpClientUtil.postJson(url, body, headers);
        log.debug("检查微信登录状态响应：{}", response);
        Map<String, Object> result = parseResponse(response);

        // 解析登录状态
        Object dataObj = result.get("data");
        if (dataObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) dataObj;
            Integer status = getStatus(data);
            if (Integer.valueOf(2).equals(status)) {
                WxAccount account = saveOrUpdateWxAccount(checkLoginDTO, data);
                // 登录成功后，仅首次登录异步拉取通讯录
                Long currentUserId = UserContext.getUserId();
                String currentWxToken = getCurrentWxToken();
                LambdaQueryWrapper<WxContact> contactWrapper = new LambdaQueryWrapper<>();
                contactWrapper.eq(WxContact::getUserId, currentUserId)
                        .eq(WxContact::getOwnerWxid, account.getWxid())
                        .last("LIMIT 1");
                Long contactCount = wxContactMapper.selectCount(contactWrapper);
                if (contactCount == null || contactCount == 0) {
                    wxContactService.syncContacts(currentUserId, account.getAppId(), account.getWxid(), currentWxToken);
                } else {
                    log.info("该账号已存在联系人，跳过通讯录同步：ownerWxid={}", account.getWxid());
                }
            }
        }
        return result;
    }

    @Override
    public List<WxAccountVO> listCurrentUserAccounts() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        LambdaQueryWrapper<WxAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WxAccount::getUserId, userId)
                .eq(WxAccount::getDeleted, 0)
                .orderByDesc(WxAccount::getCreateTime);
        List<WxAccount> accounts = baseMapper.selectList(wrapper);
        return accounts.stream().map(this::convertToVO).toList();
    }

    @Override
    public void deleteAccount(Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        int rows = baseMapper.physicalDeleteById(id, userId);
        if (rows == 0) {
            throw new BusinessException("账号不存在或无权删除");
        }
    }

    @Override
    public Map<String, Object> logout(WxLogoutDTO logoutDTO) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 校验该账号属于当前用户
        LambdaQueryWrapper<WxAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WxAccount::getUserId, userId)
                .eq(WxAccount::getAppId, logoutDTO.getAppId())
                .eq(WxAccount::getDeleted, 0);
        WxAccount account = baseMapper.selectOne(wrapper);
        if (account == null) {
            throw new BusinessException("账号不存在或无权操作");
        }

        String wxToken = getCurrentWxToken();
        String url = WX_API_BASE + "/finder/v2/api/login/logout";
        Map<String, String> headers = new HashMap<>(2);
        headers.put("X-finder-TOKEN", wxToken);

        Map<String, Object> body = new HashMap<>(2);
        body.put("appId", logoutDTO.getAppId());

        String response = HttpClientUtil.postJson(url, body, headers);
        log.debug("微信退出登录响应：{}", response);
        Map<String, Object> result = parseResponse(response);

        // 将账号状态更新为离线
        account.setStatus(0);
        baseMapper.updateById(account);

        return result;
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
     * 从响应数据中提取 status
     */
    private Integer getStatus(Map<String, Object> data) {
        Object statusObj = data.get("status");
        if (statusObj == null) {
            return null;
        }
        if (statusObj instanceof Number) {
            return ((Number) statusObj).intValue();
        }
        try {
            return Integer.parseInt(statusObj.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 登录成功后保存或更新微信账号
     *
     * @return 保存后的账号
     */
    private WxAccount saveOrUpdateWxAccount(WxCheckLoginDTO checkLoginDTO, Map<String, Object> data) {
        Long userId = UserContext.getUserId();
        String appId = checkLoginDTO.getAppId();

        // 优先从 loginInfo 中读取 wxid、alias 等字段，其次取顶层
        @SuppressWarnings("unchecked")
        Map<String, Object> loginInfo = data.get("loginInfo") instanceof Map
                ? (Map<String, Object>) data.get("loginInfo") : Collections.emptyMap();

        String wxid = getStringValue(data, loginInfo, "wxid");
        String alias = getStringValue(data, loginInfo, "alias");
        String nickName = getStringValue(data, loginInfo, "nickName");
        String headImgUrl = getStringValue(data, loginInfo, "headImgUrl");

        // 优先使用 wxid 查询，否则使用 appId
        LambdaQueryWrapper<WxAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WxAccount::getUserId, userId);
        if (StrUtil.isNotBlank(wxid)) {
            wrapper.eq(WxAccount::getWxid, wxid);
        } else {
            wrapper.eq(WxAccount::getAppId, appId);
        }
        WxAccount existAccount = baseMapper.selectOne(wrapper);

        WxAccount account = existAccount != null ? existAccount : new WxAccount();
        account.setUserId(userId);
        account.setAppId(appId);
        account.setUuid(checkLoginDTO.getUuid());
        account.setWxid(wxid);
        account.setAlias(alias);
        account.setNickName(nickName);
        account.setHeadImgUrl(headImgUrl);
        account.setRegionId(checkLoginDTO.getRegionId());
        account.setRegionName(StrUtil.isNotBlank(checkLoginDTO.getRegionName())
                ? checkLoginDTO.getRegionName()
                : REGION_MAP.get(checkLoginDTO.getRegionId()));
        account.setLoginType(StrUtil.blankToDefault(checkLoginDTO.getType(), "mac"));
        account.setStatus(1);
        account.setLoginInfo(JSON.toJSONString(data));

        if (existAccount == null) {
            baseMapper.insert(account);
        } else {
            baseMapper.updateById(account);
        }
        return account;
    }

    /**
     * 从 Map 中获取字符串值，优先从 loginInfo 读取
     */
    private String getStringValue(Map<String, Object> data, Map<String, Object> loginInfo, String key) {
        Object value = loginInfo.get(key);
        if (value == null) {
            value = data.get(key);
        }
        if (value == null) {
            return null;
        }
        String str = value.toString().trim();
        // 清洗第三方接口返回的 ``` 包裹
        if (str.startsWith("`") && str.endsWith("`")) {
            str = str.substring(1, str.length() - 1).trim();
        }
        return str;
    }

    /**
     * 转换为 VO
     */
    private WxAccountVO convertToVO(WxAccount account) {
        WxAccountVO vo = new WxAccountVO();
        BeanUtil.copyProperties(account, vo);
        return vo;
    }
}
