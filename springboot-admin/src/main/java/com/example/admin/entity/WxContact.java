package com.example.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 微信联系人实体类（好友、群聊、公众号）
 *
 * @author example
 */
@Data
@TableName("wx_contact")
public class WxContact implements Serializable {

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
     * 当前登录微信的 wxid
     */
    @TableField("owner_wxid")
    private String ownerWxid;

    /**
     * 联系人 wxid
     */
    @TableField("contact_wxid")
    private String contactWxid;

    /**
     * 联系人用户名
     */
    @TableField("user_name")
    private String userName;

    /**
     * 昵称
     */
    @TableField("nick_name")
    private String nickName;

    /**
     * 别名
     */
    private String alias;

    /**
     * 备注
     */
    private String remark;

    /**
     * 性别：0-未知，1-男，2-女
     */
    private Integer sex;

    /**
     * 个性签名
     */
    private String signature;

    /**
     * 国家
     */
    private String country;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 大头像 URL
     */
    @TableField("big_head_img_url")
    private String bigHeadImgUrl;

    /**
     * 小头像 URL
     */
    @TableField("small_head_img_url")
    private String smallHeadImgUrl;

    /**
     * 手机号（取 phoneNumList 第一个）
     */
    @TableField("phone_num")
    private String phoneNum;

    /**
     * 类型：1-好友，2-群聊，3-公众号
     */
    private Integer type;

    /**
     * 最后一条消息内容
     */
    @TableField("last_msg_content")
    private String lastMsgContent;

    /**
     * 最后一条消息时间戳
     */
    @TableField("last_msg_time")
    private Long lastMsgTime;

    /**
     * 接口返回的原始 JSON
     */
    @TableField("raw_data")
    private String rawData;

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
}
