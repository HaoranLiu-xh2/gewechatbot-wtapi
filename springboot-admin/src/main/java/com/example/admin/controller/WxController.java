package com.example.admin.controller;

import com.example.admin.common.result.Result;
import com.example.admin.dto.WxCheckLoginDTO;
import com.example.admin.dto.WxLogoutDTO;
import com.example.admin.dto.WxQrCodeDTO;
import com.example.admin.service.WxAccountService;
import com.example.admin.vo.WxAccountVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 微信账号控制器
 *
 * @author example
 */
@RestController
@RequestMapping("/api/wx")
@RequiredArgsConstructor
public class WxController {

    private final WxAccountService wxAccountService;

    /**
     * 获取微信登录二维码
     *
     * @param qrCodeDTO 请求参数
     * @return 二维码信息
     */
    @PostMapping("/login-qr")
    public Result<Map<String, Object>> getLoginQrCode(@Valid @RequestBody WxQrCodeDTO qrCodeDTO) {
        Map<String, Object> result = wxAccountService.getLoginQrCode(qrCodeDTO);
        return Result.success(result);
    }

    /**
     * 检查微信登录状态
     *
     * @param checkLoginDTO 请求参数
     * @return 登录状态信息
     */
    @PostMapping("/check-login")
    public Result<Map<String, Object>> checkLogin(@Valid @RequestBody WxCheckLoginDTO checkLoginDTO) {
        Map<String, Object> result = wxAccountService.checkLogin(checkLoginDTO);
        return Result.success(result);
    }

    /**
     * 查询当前登录用户的微信账号列表
     *
     * @return 微信账号列表
     */
    @GetMapping("/list")
    public Result<List<WxAccountVO>> list() {
        List<WxAccountVO> accounts = wxAccountService.listCurrentUserAccounts();
        return Result.success(accounts);
    }

    /**
     * 删除微信账号
     *
     * @param id 账号 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        wxAccountService.deleteAccount(id);
        return Result.success("删除成功");
    }

    /**
     * 退出微信登录
     *
     * @param logoutDTO 请求参数
     * @return 第三方接口响应
     */
    @PostMapping("/logout")
    public Result<Map<String, Object>> logout(@Valid @RequestBody WxLogoutDTO logoutDTO) {
        Map<String, Object> result = wxAccountService.logout(logoutDTO);
        return Result.success(result);
    }
}
