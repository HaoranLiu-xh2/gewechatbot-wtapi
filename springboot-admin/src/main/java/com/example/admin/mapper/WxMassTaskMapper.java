package com.example.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.admin.entity.WxMassTask;
import org.apache.ibatis.annotations.Param;

/**
 * 消息群发任务 Mapper 接口
 *
 * @author example
 */
public interface WxMassTaskMapper extends BaseMapper<WxMassTask> {

    /**
     * 根据 ID 物理删除任务
     *
     * @param id     任务 ID
     * @param userId 用户 ID（为 null 时不限制用户）
     * @return 影响行数
     */
    int physicalDeleteById(@Param("id") Long id, @Param("userId") Long userId);
}
