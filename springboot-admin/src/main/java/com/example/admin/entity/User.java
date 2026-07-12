package com.example.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.example.admin.common.constant.CommonConstant;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * @author example
 */
@Data
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（MD5 加密）
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 微信 API Token（X-finder-TOKEN）
     */
    private String token;

    /**
     * 状态：1-正常，0-禁用
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除字段：0-未删除，1-已删除
     */
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
}
