package com.example.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 微信账号实体类
 *
 * @author example
 */
@Data
@TableName("wx_account")
public class WxAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 所属系统用户 ID
     */
    private Long userId;

    /**
     * 微信应用 ID
     */
    private String appId;

    /**
     * 登录 UUID
     */
    private String uuid;

    /**
     * 微信 wxid
     */
    private String wxid;

    /**
     * 微信别名
     */
    private String alias;

    /**
     * 微信昵称
     */
    @TableField("nick_name")
    private String nickName;

    /**
     * 微信头像 URL
     */
    @TableField("head_img_url")
    private String headImgUrl;

    /**
     * 地区编码
     */
    @TableField("region_id")
    private String regionId;

    /**
     * 地区名称
     */
    @TableField("region_name")
    private String regionName;

    /**
     * 登录方式
     */
    @TableField("login_type")
    private String loginType;

    /**
     * 状态：1-在线，0-离线
     */
    private Integer status;

    /**
     * 登录返回的原始 loginInfo JSON
     */
    @TableField("login_info")
    private String loginInfo;

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
