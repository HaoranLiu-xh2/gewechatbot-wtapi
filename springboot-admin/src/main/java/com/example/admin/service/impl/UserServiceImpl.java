package com.example.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.admin.common.constant.CommonConstant;
import com.example.admin.common.exception.BusinessException;
import com.example.admin.common.page.PageQuery;
import com.example.admin.common.page.PageResult;
import com.example.admin.common.result.ResultCode;
import com.example.admin.common.utils.JwtUtil;
import com.example.admin.common.utils.UserContext;
import com.example.admin.dto.LoginDTO;
import com.example.admin.dto.RegisterDTO;
import com.example.admin.dto.UserDTO;
import com.example.admin.entity.User;
import com.example.admin.mapper.UserMapper;
import com.example.admin.service.UserService;
import com.example.admin.vo.UserVO;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户业务实现类
 *
 * @author example
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public Map<String, String> login(LoginDTO loginDTO) {
        // 根据用户名查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginDTO.getUsername());
        User user = baseMapper.selectOne(wrapper);

        // 用户不存在或已删除
        if (user == null) {
            throw new BusinessException(ResultCode.LOGIN_ERROR);
        }

        // 校验密码（前端传入明文密码，后端 MD5 后比对）
        String encryptPassword = DigestUtil.md5Hex(loginDTO.getPassword());
        if (!encryptPassword.equals(user.getPassword())) {
            throw new BusinessException(ResultCode.LOGIN_ERROR);
        }

        // 校验用户状态
        if (!CommonConstant.USER_STATUS_ENABLE.equals(user.getStatus())) {
            throw new BusinessException("用户已被禁用，请联系管理员");
        }

        // 生成 JWT Token
        String token = JwtUtil.generateToken(user.getId(), user.getUsername());

        Map<String, String> result = new HashMap<>(2);
        result.put("token", token);
        return result;
    }

    @Override
    public void register(RegisterDTO registerDTO) {
        // 校验两次密码是否一致
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }

        // 校验用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, registerDTO.getUsername());
        Long count = baseMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXIST);
        }

        // 构建用户实体
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        // 密码使用 MD5 加密存储
        user.setPassword(DigestUtil.md5Hex(registerDTO.getPassword()));
        user.setNickname(StrUtil.isBlank(registerDTO.getNickname()) ? registerDTO.getUsername() : registerDTO.getNickname());
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());
        // 默认状态为正常
        user.setStatus(CommonConstant.USER_STATUS_ENABLE);

        baseMapper.insert(user);
    }

    @Override
    public PageResult<UserVO> pageList(PageQuery pageQuery) {
        // 构建分页对象
        Page<User> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());

        // 构建查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(pageQuery.getKeyword()), User::getUsername, pageQuery.getKeyword())
                .or()
                .like(StrUtil.isNotBlank(pageQuery.getKeyword()), User::getNickname, pageQuery.getKeyword())
                .orderByDesc(User::getCreateTime);

        // 执行分页查询
        Page<User> userPage = baseMapper.selectPage(page, wrapper);

        // 转换为 VO 分页结果
        Page<UserVO> voPage = new Page<>();
        BeanUtil.copyProperties(userPage, voPage);
        voPage.setRecords(userPage.getRecords().stream().map(this::convertToVO).toList());

        return PageResult.build(voPage);
    }

    @Override
    public UserVO getById(Long id) {
        User user = baseMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return convertToVO(user);
    }

    @Override
    public UserVO getCurrentUser() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return getById(userId);
    }

    @Override
    public void updateToken(String token) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId).set(User::getToken, token);
        baseMapper.update(null, updateWrapper);
    }

    @Override
    public void add(UserDTO userDTO) {
        // 校验用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, userDTO.getUsername());
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXIST);
        }

        User user = new User();
        BeanUtil.copyProperties(userDTO, user);
        // 新增时若未传入密码，默认密码为 123456
        if (StrUtil.isBlank(userDTO.getPassword())) {
            user.setPassword(DigestUtil.md5Hex("123456"));
        } else {
            user.setPassword(DigestUtil.md5Hex(userDTO.getPassword()));
        }
        // 默认状态为正常
        if (user.getStatus() == null) {
            user.setStatus(CommonConstant.USER_STATUS_ENABLE);
        }

        baseMapper.insert(user);
    }

    @Override
    public void update(UserDTO userDTO) {
        if (userDTO.getId() == null) {
            throw new BusinessException("用户 ID 不能为空");
        }

        // 查询用户是否存在
        User existUser = baseMapper.selectById(userDTO.getId());
        if (existUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 校验用户名是否与其他用户重复
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, userDTO.getUsername())
                .ne(User::getId, userDTO.getId());
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXIST);
        }

        // 构建更新对象
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userDTO.getId())
                .set(User::getUsername, userDTO.getUsername())
                .set(User::getNickname, userDTO.getNickname())
                .set(User::getPhone, userDTO.getPhone())
                .set(User::getEmail, userDTO.getEmail())
                .set(User::getStatus, userDTO.getStatus());

        // 如果传入了密码，则更新密码
        if (StrUtil.isNotBlank(userDTO.getPassword())) {
            updateWrapper.set(User::getPassword, DigestUtil.md5Hex(userDTO.getPassword()));
        }

        baseMapper.update(null, updateWrapper);
    }

    @Override
    public void delete(Long id) {
        User user = baseMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        baseMapper.deleteById(id);
    }

    /**
     * 将 User 实体转换为 UserVO
     */
    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        BeanUtil.copyProperties(user, vo);
        return vo;
    }
}
