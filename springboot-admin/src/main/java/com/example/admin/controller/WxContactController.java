package com.example.admin.controller;

import com.example.admin.common.result.Result;
import com.example.admin.entity.WxContact;
import com.example.admin.service.WxContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 微信联系人控制器
 *
 * @author example
 */
@RestController
@RequestMapping("/api/wx/contact")
@RequiredArgsConstructor
public class WxContactController {

    private final WxContactService wxContactService;

    /**
     * 查询当前登录用户的联系人列表
     *
     * @param ownerWxid 当前登录微信的 wxid
     * @return 联系人列表
     */
    @GetMapping("/list")
    public Result<List<WxContact>> list(@RequestParam String ownerWxid) {
        List<WxContact> contacts = wxContactService.listContacts(ownerWxid);
        return Result.success(contacts);
    }
}
