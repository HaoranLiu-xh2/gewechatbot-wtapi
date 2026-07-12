package com.example.admin.common.page;

import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 统一分页查询参数
 *
 * @author example
 */
@Data
public class PageQuery {

    /**
     * 当前页码（默认第 1 页）
     */
    @Min(value = 1, message = "页码必须大于等于 1")
    private Long pageNum = 1L;

    /**
     * 每页大小（默认 10 条）
     */
    @Min(value = 1, message = "每页条数必须大于等于 1")
    private Long pageSize = 10L;

    /**
     * 关键字搜索
     */
    private String keyword;
}
