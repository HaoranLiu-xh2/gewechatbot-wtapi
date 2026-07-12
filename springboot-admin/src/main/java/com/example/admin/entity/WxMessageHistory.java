package com.example.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 微信消息归档表
 *
 * @author example
 */
@Data
@TableName("wx_message_history")
public class WxMessageHistory {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.INPUT)
    private Long id;

    /**
     * 所属系统用户 ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 设备 ID
     */
    @TableField("app_id")
    private String appId;

    /**
     * 所属微信的 wxid
     */
    @TableField("wxid")
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
     * 消息类型：1-文本
     */
    @TableField("msg_type")
    private Integer msgType;

    /**
     * 消息内容
     */
    @TableField("content")
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
     * 归档时间
     */
    @TableField("archive_time")
    private LocalDateTime archiveTime;
}
