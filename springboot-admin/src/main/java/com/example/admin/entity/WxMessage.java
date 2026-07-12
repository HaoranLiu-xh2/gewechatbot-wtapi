package com.example.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 微信消息实体类
 *
 * @author example
 */
@Data
@TableName("wx_message")
public class WxMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID（序列化为字符串，防止前端精度丢失）
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 所属系统用户 ID
     */
    private Long userId;

    /**
     * 设备 ID
     */
    private String appId;

    /**
     * 所属微信的 wxid
     */
    private String wxid;

    /**
     * 消息 ID
     */
    @TableField("msg_id")
    private Long msgId;

    /**
     * 新消息 ID
     */
    @TableField("new_msg_id")
    private Long newMsgId;

    /**
     * 发送人 wxid
     */
    @TableField("from_wxid")
    private String fromWxid;

    /**
     * 接收人 wxid
     */
    @TableField("to_wxid")
    private String toWxid;

    /**
     * 消息类型：1-文本，3-图片，6-文件，43-视频
     */
    @TableField("msg_type")
    private Integer msgType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息发送时间戳
     */
    @TableField("msg_time")
    private Long msgTime;

    /**
     * 原始消息 JSON
     */
    @TableField("raw_data")
    private String rawData;

    /**
     * 本地创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
