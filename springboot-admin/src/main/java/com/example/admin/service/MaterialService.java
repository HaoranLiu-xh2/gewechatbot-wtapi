package com.example.admin.service;

import com.example.admin.common.page.PageQuery;
import com.example.admin.common.page.PageResult;
import com.example.admin.dto.MaterialDTO;
import com.example.admin.vo.MaterialVO;

/**
 * 素材库服务接口
 *
 * @author example
 */
public interface MaterialService {

    /**
     * 分页查询素材列表
     *
     * @param pageQuery 分页参数
     * @param type      素材类型
     * @return 分页结果
     */
    PageResult<MaterialVO> pageList(PageQuery pageQuery, Integer type);

    /**
     * 根据 ID 查询素材详情
     *
     * @param id 素材 ID
     * @return 素材详情
     */
    MaterialVO getById(Long id);

    /**
     * 新增素材
     *
     * @param materialDTO 素材信息
     */
    void add(MaterialDTO materialDTO);

    /**
     * 修改素材
     *
     * @param materialDTO 素材信息
     */
    void update(MaterialDTO materialDTO);

    /**
     * 删除素材
     *
     * @param id 素材 ID
     */
    void delete(Long id);

    /**
     * 将聊天消息保存为素材
     *
     * @param messageId 消息 ID
     * @return 素材 ID
     */
    Long saveFromMessage(Long messageId);
}
