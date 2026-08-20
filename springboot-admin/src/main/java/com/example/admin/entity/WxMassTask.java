package com.example.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 消息群发任务实体类
 *
 * @author example
 */
@Data
@TableName("wx_mass_task")
public class WxMassTask implements Serializable {

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
     * 微信应用 ID
     */
    private String appId;

    /**
     * 当前登录微信的 wxid
     */
    private String ownerWxid;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 目标类型：1-好友，2-群，3-混合
     */
    private Integer targetType;

    /**
     * 消息类型：1-文本
     */
    private Integer msgType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 发送方式：1-立即发送，2-定时发送
     */
    private Integer sendType;

    /**
     * 发送日期区间开始
     */
    private LocalDate startDate;

    /**
     * 发送日期区间结束
     */
    private LocalDate endDate;

    /**
     * 每天发送时段开始
     */
    private LocalTime startTime;

    /**
     * 每天发送时段结束
     */
    private LocalTime endTime;

    /**
     * 消息间隔秒数
     */
    private Integer intervalSeconds;

    /**
     * 定时发送时间
     */
    private LocalDateTime scheduleTime;

    /**
     * 状态：0-待执行，1-执行中，2-已完成，3-已暂停，4-已取消，5-已失败
     */
    private Integer status;

    /**
     * 总记录数
     */
    private Integer totalCount;

    /**
     * 成功数
     */
    private Integer successCount;

    /**
     * 失败数
     */
    private Integer failCount;

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
