package com.example.admin.controller;

import com.example.admin.common.result.Result;
import com.example.admin.dto.LoginDTO;
import com.example.admin.dto.RegisterDTO;
import com.example.admin.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 认证控制器（登录、注册）
 *
 * @author example
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * 用户登录
     *
     * @param loginDTO 登录参数
     * @return Token 信息
     */
    @PostMapping("/login")
    public Result<Map<String, String>> login(@Valid @RequestBody LoginDTO loginDTO) {
        Map<String, String> tokenMap = userService.login(loginDTO);
        return Result.success("登录成功", tokenMap);
    }

    /**
     * 用户注册
     *
     * @param registerDTO 注册参数
     * @return 操作结果
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO registerDTO) {
        userService.register(registerDTO);
        return Result.success("注册成功");
    }
}
