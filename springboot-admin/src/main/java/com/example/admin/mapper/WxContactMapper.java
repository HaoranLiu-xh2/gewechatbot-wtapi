package com.example.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.admin.entity.WxContact;
import org.apache.ibatis.annotations.Mapper;

/**
 * 微信联系人数据访问层
 *
 * @author example
 */
@Mapper
public interface WxContactMapper extends BaseMapper<WxContact> {
}
