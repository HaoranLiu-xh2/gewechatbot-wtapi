package com.example.admin.common.page;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 统一分页返回结果
 *
 * @param <T> 分页数据类型
 * @author example
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Long pageNum;

    /**
     * 每页大小
     */
    private Long pageSize;

    /**
     * 总页数
     */
    private Long pages;

    /**
     * 分页数据列表
     */
    private List<T> list;

    public PageResult() {
    }

    public PageResult(Long total, Long pageNum, Long pageSize, List<T> list) {
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.list = list;
        this.pages = pageSize == 0 ? 0L : (total + pageSize - 1) / pageSize;
    }

    /**
     * 从 MyBatis Plus 分页对象构建返回结果
     */
    public static <T> PageResult<T> build(IPage<T> page) {
        if (page == null) {
            return new PageResult<>(0L, 1L, 10L, Collections.emptyList());
        }
        return new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    /**
     * 构建空分页结果
     */
    public static <T> PageResult<T> empty(long pageNum, long pageSize) {
        return new PageResult<>(0L, pageNum, pageSize, Collections.emptyList());
    }
}
