package com.example.admin.service;

import com.example.admin.common.page.PageResult;
import com.example.admin.dto.LoginDTO;
import com.example.admin.dto.RegisterDTO;
import com.example.admin.dto.UserDTO;
import com.example.admin.vo.UserVO;
import com.example.admin.common.page.PageQuery;

import java.util.Map;

/**
 * 用户业务接口
 *
 * @author example
 */
public interface UserService {

    /**
     * 用户登录
     *
     * @param loginDTO 登录参数
     * @return Token 信息
     */
    Map<String, String> login(LoginDTO loginDTO);

    /**
     * 用户注册
     *
     * @param registerDTO 注册参数
     */
    void register(RegisterDTO registerDTO);

    /**
     * 分页查询用户列表
     *
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<UserVO> pageList(PageQuery pageQuery);

    /**
     * 根据 ID 查询用户详情
     *
     * @param id 用户 ID
     * @return 用户详情
     */
    UserVO getById(Long id);

    /**
     * 查询当前登录用户详情
     *
     * @return 当前用户详情
     */
    UserVO getCurrentUser();

    /**
     * 更新当前登录用户的微信 API Token
     *
     * @param token 微信 API Token
     */
    void updateToken(String token);

    /**
     * 新增用户
     *
     * @param userDTO 用户信息
     */
    void add(UserDTO userDTO);

    /**
     * 修改用户
     *
     * @param userDTO 用户信息
     */
    void update(UserDTO userDTO);

    /**
     * 删除用户
     *
     * @param id 用户 ID
     */
    void delete(Long id);
}
