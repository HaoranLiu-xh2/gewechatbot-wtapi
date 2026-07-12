package com.example.admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 微信账号视图对象
 *
 * @author example
 */
@Data
public class WxAccountVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

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
    private String nickName;

    /**
     * 微信头像 URL
     */
    private String headImgUrl;

    /**
     * 地区编码
     */
    private String regionId;

    /**
     * 地区名称
     */
    private String regionName;

    /**
     * 登录方式
     */
    private String loginType;

    /**
     * 状态：1-在线，0-离线
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
