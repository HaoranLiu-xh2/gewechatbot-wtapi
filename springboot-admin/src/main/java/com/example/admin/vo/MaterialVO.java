package com.example.admin.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 素材库 VO
 *
 * @author example
 */
@Data
public class MaterialVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 所属系统用户 ID
     */
    private Long userId;

    /**
     * 素材名称
     */
    private String name;

    /**
     * 素材类型：1-图片，2-文本，3-小程序，4-视频，5-链接，6-文件
     */
    private Integer type;

    /**
     * 素材内容（JSON 格式）
     */
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
