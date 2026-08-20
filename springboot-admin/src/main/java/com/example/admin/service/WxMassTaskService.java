package com.example.admin.service;

import com.example.admin.common.page.PageQuery;
import com.example.admin.common.page.PageResult;
import com.example.admin.dto.WxMassTaskCreateDTO;
import com.example.admin.entity.WxMassTask;
import com.example.admin.entity.WxMassTaskRecord;

/**
 * 消息群发任务业务接口
 *
 * @author example
 */
public interface WxMassTaskService {

    /**
     * 创建群发任务
     *
     * @param dto 任务参数
     * @return 任务 ID
     */
    Long createTask(WxMassTaskCreateDTO dto);

    /**
     * 执行任务（立即或定时触发）
     *
     * @param taskId 任务 ID
     */
    void executeTask(Long taskId);

    /**
     * 暂停任务
     *
     * @param taskId 任务 ID
     */
    void pauseTask(Long taskId);

    /**
     * 取消任务
     *
     * @param taskId 任务 ID
     */
    void cancelTask(Long taskId);

    /**
     * 分页查询任务列表
     *
     * @param query 分页参数
     * @return 分页结果
     */
    PageResult<WxMassTask> pageTasks(PageQuery query);

    /**
     * 查询任务详情
     *
     * @param taskId 任务 ID
     * @return 任务详情
     */
    WxMassTask getTaskDetail(Long taskId);

    /**
     * 删除任务
     *
     * @param taskId 任务 ID
     */
    void deleteTask(Long taskId);

    /**
     * 分页查询任务发送记录
     *
     * @param taskId 任务 ID
     * @param query  分页参数
     * @return 分页结果
     */
    PageResult<WxMassTaskRecord> pageTaskRecords(Long taskId, PageQuery query);
}
