package com.example.admin.controller;

import com.example.admin.common.page.PageQuery;
import com.example.admin.common.page.PageResult;
import com.example.admin.common.result.Result;
import com.example.admin.dto.WxMassTaskCreateDTO;
import com.example.admin.entity.WxMassTask;
import com.example.admin.entity.WxMassTaskRecord;
import com.example.admin.service.WxMassTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 消息群发任务控制器
 *
 * @author example
 */
@RestController
@RequestMapping("/api/mass-task")
@RequiredArgsConstructor
public class WxMassTaskController {

    private final WxMassTaskService wxMassTaskService;

    /**
     * 创建群发任务
     *
     * @param dto 任务参数
     * @return 任务 ID
     */
    @PostMapping("/create")
    public Result<Long> create(@Valid @RequestBody WxMassTaskCreateDTO dto) {
        Long taskId = wxMassTaskService.createTask(dto);
        return Result.success(taskId);
    }

    /**
     * 分页查询群发任务列表
     *
     * @param query 分页参数
     * @return 任务列表
     */
    @GetMapping("/page")
    public Result<PageResult<WxMassTask>> page(PageQuery query) {
        PageResult<WxMassTask> result = wxMassTaskService.pageTasks(query);
        return Result.success(result);
    }

    /**
     * 查询任务详情
     *
     * @param id 任务 ID
     * @return 任务详情
     */
    @GetMapping("/{id}")
    public Result<WxMassTask> detail(@PathVariable Long id) {
        WxMassTask task = wxMassTaskService.getTaskDetail(id);
        return Result.success(task);
    }

    /**
     * 分页查询任务发送记录
     *
     * @param id    任务 ID
     * @param query 分页参数
     * @return 发送记录列表
     */
    @GetMapping("/{id}/records")
    public Result<PageResult<WxMassTaskRecord>> records(@PathVariable Long id, PageQuery query) {
        PageResult<WxMassTaskRecord> result = wxMassTaskService.pageTaskRecords(id, query);
        return Result.success(result);
    }

    /**
     * 暂停任务
     *
     * @param id 任务 ID
     * @return 操作结果
     */
    @PostMapping("/{id}/pause")
    public Result<Void> pause(@PathVariable Long id) {
        wxMassTaskService.pauseTask(id);
        return Result.success("暂停成功");
    }

    /**
     * 取消任务
     *
     * @param id 任务 ID
     * @return 操作结果
     */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        wxMassTaskService.cancelTask(id);
        return Result.success("取消成功");
    }

    /**
     * 删除任务
     *
     * @param id 任务 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        wxMassTaskService.deleteTask(id);
        return Result.success("删除成功");
    }
}
