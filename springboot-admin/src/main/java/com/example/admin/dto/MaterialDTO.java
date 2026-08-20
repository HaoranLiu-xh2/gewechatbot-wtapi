package com.example.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 素材库 DTO
 *
 * @author example
 */
@Data
public class MaterialDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 素材 ID（新增时为空）
     */
    private Long id;

    /**
     * 素材名称
     */
    @NotBlank(message = "素材名称不能为空")
    private String name;

    /**
     * 素材类型：1-图片，2-文本，3-小程序，4-视频，5-链接，6-文件
     */
    @NotNull(message = "素材类型不能为空")
    private Integer type;

    /**
     * 素材内容（JSON 格式）
     */
    @NotBlank(message = "素材内容不能为空")
    private String content;
}
