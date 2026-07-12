package com.example.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.admin.entity.WxMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 微信消息数据访问层
 *
 * @author example
 */
@Mapper
public interface WxMessageMapper extends BaseMapper<WxMessage> {

    /**
     * 归档指定时间之前的消息到历史表，并返回迁移的记录数
     *
     * @param beforeTime 时间戳（秒）
     * @return 迁移记录数
     */
    int archiveMessages(@Param("beforeTime") Long beforeTime);

    /**
     * 删除指定时间之前的已归档消息
     *
     * @param beforeTime 时间戳（秒）
     * @return 删除记录数
     */
    int deleteArchivedMessages(@Param("beforeTime") Long beforeTime);
}
