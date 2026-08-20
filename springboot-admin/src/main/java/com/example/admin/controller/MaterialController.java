package com.example.admin.controller;

import com.example.admin.common.page.PageQuery;
import com.example.admin.common.page.PageResult;
import com.example.admin.common.result.Result;
import com.example.admin.dto.MaterialDTO;
import com.example.admin.service.MaterialService;
import com.example.admin.vo.MaterialVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 素材库控制器
 * <p>
 * 群发任务依赖素材选择能力，一并提供 CRUD 接口。
 *
 * @author example
 */
@RestController
@RequestMapping("/api/material")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    /**
     * 分页查询素材列表
     *
     * @param pageQuery 分页参数
     * @param type      素材类型
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<PageResult<MaterialVO>> page(PageQuery pageQuery, Integer type) {
        PageResult<MaterialVO> pageResult = materialService.pageList(pageQuery, type);
        return Result.success(pageResult);
    }

    /**
     * 根据 ID 查询素材详情
     *
     * @param id 素材 ID
     * @return 素材详情
     */
    @GetMapping("/{id}")
    public Result<MaterialVO> getById(@PathVariable Long id) {
        MaterialVO materialVO = materialService.getById(id);
        return Result.success(materialVO);
    }

    /**
     * 新增素材
     *
     * @param materialDTO 素材信息
     * @return 操作结果
     */
    @PostMapping
    public Result<Void> add(@Valid @RequestBody MaterialDTO materialDTO) {
        materialService.add(materialDTO);
        return Result.success("新增成功");
    }

    /**
     * 修改素材
     *
     * @param materialDTO 素材信息
     * @return 操作结果
     */
    @PutMapping
    public Result<Void> update(@Valid @RequestBody MaterialDTO materialDTO) {
        materialService.update(materialDTO);
        return Result.success("修改成功");
    }

    /**
     * 删除素材
     *
     * @param id 素材 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        materialService.delete(id);
        return Result.success("删除成功");
    }

    /**
     * 将聊天消息保存为素材
     *
     * @param messageId 消息 ID
     * @return 素材 ID（字符串，避免前端 Long 精度丢失）
     */
    @PostMapping("/save-from-message")
    public Result<String> saveFromMessage(@RequestParam Long messageId) {
        Long materialId = materialService.saveFromMessage(messageId);
        return Result.success("保存成功", String.valueOf(materialId));
    }
}
