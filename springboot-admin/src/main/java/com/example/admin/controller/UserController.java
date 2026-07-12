package com.example.admin.controller;

import com.example.admin.common.page.PageQuery;
import com.example.admin.common.page.PageResult;
import com.example.admin.common.result.Result;
import cn.hutool.core.util.StrUtil;
import com.example.admin.dto.UserDTO;
import com.example.admin.service.UserService;
import com.example.admin.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户管理控制器
 *
 * @author example
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 分页查询用户列表
     *
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<PageResult<UserVO>> page(PageQuery pageQuery) {
        PageResult<UserVO> pageResult = userService.pageList(pageQuery);
        return Result.success(pageResult);
    }

    /**
     * 根据 ID 查询用户详情
     *
     * @param id 用户 ID
     * @return 用户详情
     */
    @GetMapping("/{id}")
    public Result<UserVO> getById(@PathVariable Long id) {
        UserVO userVO = userService.getById(id);
        return Result.success(userVO);
    }

    /**
     * 查询当前登录用户详情
     *
     * @return 当前用户详情
     */
    @GetMapping("/current")
    public Result<UserVO> getCurrentUser() {
        UserVO userVO = userService.getCurrentUser();
        return Result.success(userVO);
    }

    /**
     * 更新当前登录用户的微信 API Token
     *
     * @param tokenMap Token 参数
     * @return 操作结果
     */
    @PutMapping("/token")
    public Result<Void> updateToken(@RequestBody Map<String, String> tokenMap) {
        String token = tokenMap.get("token");
        if (StrUtil.isBlank(token)) {
            return Result.error("Token 不能为空");
        }
        userService.updateToken(token);
        return Result.success("Token 更新成功");
    }

    /**
     * 新增用户
     *
     * @param userDTO 用户信息
     * @return 操作结果
     */
    @PostMapping
    public Result<Void> add(@Valid @RequestBody UserDTO userDTO) {
        userService.add(userDTO);
        return Result.success("新增成功");
    }

    /**
     * 修改用户
     *
     * @param userDTO 用户信息
     * @return 操作结果
     */
    @PutMapping
    public Result<Void> update(@Valid @RequestBody UserDTO userDTO) {
        userService.update(userDTO);
        return Result.success("修改成功");
    }

    /**
     * 删除用户
     *
     * @param id 用户 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success("删除成功");
    }
}
