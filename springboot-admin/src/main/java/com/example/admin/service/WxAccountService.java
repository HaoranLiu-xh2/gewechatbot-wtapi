package com.example.admin.service;

import com.example.admin.dto.WxCheckLoginDTO;
import com.example.admin.dto.WxLogoutDTO;
import com.example.admin.dto.WxQrCodeDTO;
import com.example.admin.vo.WxAccountVO;

import java.util.List;
import java.util.Map;

/**
 * 微信账号业务接口
 *
 * @author example
 */
public interface WxAccountService {

    /**
     * 获取微信登录二维码
     *
     * @param qrCodeDTO 请求参数
     * @return 二维码信息
     */
    Map<String, Object> getLoginQrCode(WxQrCodeDTO qrCodeDTO);

    /**
     * 检查微信登录状态
     *
     * @param checkLoginDTO 请求参数
     * @return 登录状态信息
     */
    Map<String, Object> checkLogin(WxCheckLoginDTO checkLoginDTO);

    /**
     * 查询当前登录用户的微信账号列表
     *
     * @return 微信账号列表
     */
    List<WxAccountVO> listCurrentUserAccounts();

    /**
     * 根据 ID 删除微信账号
     *
     * @param id 账号 ID
     */
    void deleteAccount(Long id);

    /**
     * 退出微信登录
     *
     * @param logoutDTO 请求参数
     * @return 第三方接口响应
     */
    Map<String, Object> logout(WxLogoutDTO logoutDTO);
}
