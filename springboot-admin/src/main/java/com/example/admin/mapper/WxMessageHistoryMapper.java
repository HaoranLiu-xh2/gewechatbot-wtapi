package com.example.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.admin.entity.WxMessageHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 微信消息归档表 Mapper
 *
 * @author example
 */
@Mapper
public interface WxMessageHistoryMapper extends BaseMapper<WxMessageHistory> {
}
