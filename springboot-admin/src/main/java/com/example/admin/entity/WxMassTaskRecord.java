package com.example.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息群发任务记录实体类
 *
 * @author example
 */
@Data
@TableName("wx_mass_task_record")
public class WxMassTaskRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID（序列化为字符串，防止前端精度丢失）
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 任务 ID
     */
    private Long taskId;

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
     * 联系人 wxid
     */
    private String contactWxid;

    /**
     * 联系人类型：1-好友，2-群
     */
    private Integer contactType;

    /**
     * 联系人昵称
     */
    private String nickName;

    /**
     * 状态：0-待发送，1-成功，2-失败
     */
    private Integer status;

    /**
     * 失败原因
     */
    private String errorMsg;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

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
