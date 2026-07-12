package com.example.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.admin.entity.WxAccount;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 微信账号数据访问层
 *
 * @author example
 */
@Mapper
public interface WxAccountMapper extends BaseMapper<WxAccount> {

    /**
     * 物理删除当前用户的微信账号
     *
     * @param id     账号 ID
     * @param userId 用户 ID
     * @return 影响行数
     */
    @Delete("DELETE FROM wx_account WHERE id = #{id} AND user_id = #{userId}")
    int physicalDeleteById(@Param("id") Long id, @Param("userId") Long userId);
}
