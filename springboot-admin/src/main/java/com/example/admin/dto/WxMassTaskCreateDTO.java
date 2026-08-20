package com.example.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 创建消息群发任务请求参数
 *
 * @author example
 */
@Data
public class WxMassTaskCreateDTO {

    /**
     * 任务名称
     */
    @NotBlank(message = "任务名称不能为空")
    private String name;

    /**
     * 微信应用 ID
     */
    @NotBlank(message = "请选择微信账号")
    private String appId;

    /**
     * 目标类型：1-好友，2-群，3-混合
     */
    @NotNull(message = "请选择群发对象类型")
    private Integer targetType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 素材库 ID（优先于 content）
     */
    private Long materialId;

    /**
     * 发送方式：1-立即发送，2-定时发送
     */
    @NotNull(message = "请选择发送方式")
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
    @NotNull(message = "请设置消息间隔秒数")
    private Integer intervalSeconds;

    /**
     * 定时发送时间（定时发送时必填）
     */
    private LocalDateTime scheduleTime;

    /**
     * 联系人 wxid 列表
     */
    @NotEmpty(message = "请至少选择一个联系人")
    private List<String> contactWxids;
}
