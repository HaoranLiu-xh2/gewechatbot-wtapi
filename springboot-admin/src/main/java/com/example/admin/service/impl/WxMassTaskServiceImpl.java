package com.example.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.admin.common.constant.CommonConstant;
import com.example.admin.common.exception.BusinessException;
import com.example.admin.common.page.PageQuery;
import com.example.admin.common.page.PageResult;
import com.example.admin.common.result.ResultCode;
import com.example.admin.common.utils.UserContext;
import com.example.admin.dto.WxMassTaskCreateDTO;
import com.example.admin.dto.WxSendFileDTO;
import com.example.admin.dto.WxSendImageDTO;
import com.example.admin.dto.WxSendTextDTO;
import com.example.admin.dto.WxSendVideoDTO;
import com.example.admin.entity.Material;
import com.example.admin.entity.WxAccount;
import com.example.admin.entity.WxContact;
import com.example.admin.entity.WxMassTask;
import com.example.admin.entity.WxMassTaskRecord;
import com.example.admin.mapper.MaterialMapper;
import com.example.admin.mapper.WxAccountMapper;
import com.example.admin.mapper.WxContactMapper;
import com.example.admin.mapper.WxMassTaskMapper;
import com.example.admin.mapper.WxMassTaskRecordMapper;
import com.example.admin.service.WxMassTaskService;
import com.example.admin.service.WxMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 消息群发任务业务实现类
 *
 * @author example
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WxMassTaskServiceImpl extends ServiceImpl<WxMassTaskMapper, WxMassTask> implements WxMassTaskService {

    /**
     * Redis 延迟队列 Key
     */
    private static final String REDIS_DELAY_QUEUE_KEY = "wx:mass:task:delay";

    /**
     * 分布式锁 Key 前缀
     */
    private static final String REDIS_LOCK_KEY_PREFIX = "wx:mass:task:lock:";

    /**
     * 状态：待执行
     */
    private static final int STATUS_PENDING = 0;

    /**
     * 状态：执行中
     */
    private static final int STATUS_RUNNING = 1;

    /**
     * 状态：已完成
     */
    private static final int STATUS_COMPLETED = 2;

    /**
     * 状态：已暂停
     */
    private static final int STATUS_PAUSED = 3;

    /**
     * 状态：已取消
     */
    private static final int STATUS_CANCELLED = 4;

    /**
     * 状态：已失败
     */
    private static final int STATUS_FAILED = 5;

    /**
     * 状态：待发送
     */
    private static final int RECORD_STATUS_PENDING = 0;

    /**
     * 状态：发送成功
     */
    private static final int RECORD_STATUS_SUCCESS = 1;

    /**
     * 状态：发送失败
     */
    private static final int RECORD_STATUS_FAILED = 2;

    /**
     * 发送方式：立即发送
     */
    private static final int SEND_TYPE_IMMEDIATE = 1;

    /**
     * 发送方式：定时发送
     */
    private static final int SEND_TYPE_SCHEDULED = 2;

    /**
     * 素材类型：图片
     */
    private static final int MATERIAL_TYPE_IMAGE = 1;

    /**
     * 素材类型：文本
     */
    private static final int MATERIAL_TYPE_TEXT = 2;

    /**
     * 素材类型：小程序
     */
    private static final int MATERIAL_TYPE_MINI_APP = 3;

    /**
     * 素材类型：视频
     */
    private static final int MATERIAL_TYPE_VIDEO = 4;

    /**
     * 素材类型：链接
     */
    private static final int MATERIAL_TYPE_LINK = 5;

    /**
     * 素材类型：文件
     */
    private static final int MATERIAL_TYPE_FILE = 6;

    private final WxMassTaskMapper wxMassTaskMapper;

    private final WxMassTaskRecordMapper wxMassTaskRecordMapper;

    private final WxAccountMapper wxAccountMapper;

    private final WxContactMapper wxContactMapper;


    private final MaterialMapper materialMapper;

    private final WxMessageService wxMessageService;

    private final StringRedisTemplate redisTemplate;

    @Qualifier("wxMassTaskExecutor")
    private final AsyncTaskExecutor wxMassTaskExecutor;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTask(WxMassTaskCreateDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 校验微信账号归属
        WxAccount account = validateAccount(userId, dto.getAppId());
        if (StrUtil.isBlank(account.getWxid())) {
            throw new BusinessException("所选微信账号缺少 wxid，无法创建任务");
        }

        // 校验联系人
        List<WxContact> contacts = validateContacts(userId, account.getWxid(), dto.getTargetType(), dto.getContactWxids());

        // 校验并解析素材
        MaterialContent materialContent = resolveMaterial(userId, dto.getMaterialId(), dto.getContent());

        // 校验时段参数
        validateTimeRange(dto.getStartTime(), dto.getEndTime());

        // 校验定时发送时间
        if (SEND_TYPE_SCHEDULED == dto.getSendType()) {
            if (dto.getScheduleTime() == null) {
                throw new BusinessException("定时发送时请设置发送时间");
            }
            if (dto.getScheduleTime().isBefore(LocalDateTime.now())) {
                throw new BusinessException("定时发送时间不能早于当前时间");
            }
        }

        // 保存任务
        WxMassTask task = new WxMassTask();
        task.setUserId(userId);
        task.setAppId(dto.getAppId());
        task.setOwnerWxid(account.getWxid());
        task.setName(dto.getName());
        task.setTargetType(dto.getTargetType());
        task.setMsgType(materialContent.getMsgType());
        task.setContent(materialContent.getContent());
        task.setSendType(dto.getSendType());
        task.setStartDate(dto.getStartDate());
        task.setEndDate(dto.getEndDate());
        task.setStartTime(dto.getStartTime());
        task.setEndTime(dto.getEndTime());
        task.setIntervalSeconds(dto.getIntervalSeconds());
        task.setScheduleTime(dto.getScheduleTime());
        task.setStatus(STATUS_PENDING);
        task.setTotalCount(contacts.size());
        task.setSuccessCount(0);
        task.setFailCount(0);
        wxMassTaskMapper.insert(task);

        // 保存任务记录
        List<WxMassTaskRecord> records = new ArrayList<>(contacts.size());
        for (WxContact contact : contacts) {
            WxMassTaskRecord record = new WxMassTaskRecord();
            record.setTaskId(task.getId());
            record.setUserId(userId);
            record.setAppId(dto.getAppId());
            record.setOwnerWxid(account.getWxid());
            record.setContactWxid(contact.getContactWxid());
            record.setContactType(contact.getType());
            record.setNickName(StrUtil.isNotBlank(contact.getRemark()) ? contact.getRemark()
                    : StrUtil.isNotBlank(contact.getNickName()) ? contact.getNickName() : contact.getContactWxid());
            record.setStatus(RECORD_STATUS_PENDING);
            records.add(record);
        }
        for (WxMassTaskRecord record : records) {
            wxMassTaskRecordMapper.insert(record);
        }

        // 立即发送直接触发执行；定时发送加入 Redis 延迟队列
        if (SEND_TYPE_IMMEDIATE == dto.getSendType()) {
            WxMassTask updateTask = new WxMassTask();
            updateTask.setId(task.getId());
            updateTask.setStatus(STATUS_RUNNING);
            wxMassTaskMapper.updateById(updateTask);
            // 通过线程池异步执行，避免 @Async 同类自调用失效导致阻塞 HTTP 响应
            wxMassTaskExecutor.submit(() -> executeTask(task.getId()));
        } else {
            long score = dto.getScheduleTime().atZone(ZoneId.systemDefault()).toEpochSecond();
            redisTemplate.opsForZSet().add(REDIS_DELAY_QUEUE_KEY, String.valueOf(task.getId()), score);
            log.info("群发任务已加入 Redis 延迟队列：taskId={}，执行时间={}", task.getId(), dto.getScheduleTime());
        }

        return task.getId();
    }

    @Override
    public void executeTask(Long taskId) {
        // 获取分布式锁，避免多实例重复执行
        String lockKey = REDIS_LOCK_KEY_PREFIX + taskId;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.MINUTES);
        if (Boolean.FALSE.equals(locked)) {
            log.warn("群发任务正在执行中，跳过：taskId={}", taskId);
            return;
        }

        try {
            WxMassTask task = wxMassTaskMapper.selectById(taskId);
            if (task == null || task.getDeleted() == 1) {
                log.warn("群发任务不存在或已删除：taskId={}", taskId);
                return;
            }
            if (STATUS_CANCELLED == task.getStatus() || STATUS_PAUSED == task.getStatus()) {
                log.info("群发任务已暂停或取消，停止执行：taskId={}，status={}", taskId, task.getStatus());
                return;
            }

            // 设置当前用户上下文，供后续消息发送服务校验使用
            UserContext.setUser(task.getUserId(), "");

            // 更新任务为执行中
            WxMassTask updateTask = new WxMassTask();
            updateTask.setId(taskId);
            updateTask.setStatus(STATUS_RUNNING);
            wxMassTaskMapper.updateById(updateTask);

            // 查询待发送记录
            LambdaQueryWrapper<WxMassTaskRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(WxMassTaskRecord::getTaskId, taskId)
                    .eq(WxMassTaskRecord::getStatus, RECORD_STATUS_PENDING)
                    .orderByAsc(WxMassTaskRecord::getId);
            List<WxMassTaskRecord> pendingRecords = wxMassTaskRecordMapper.selectList(wrapper);
            if (pendingRecords.isEmpty()) {
                finishTask(taskId);
                return;
            }

            log.info("开始执行群发任务：taskId={}，待发送记录数={}", taskId, pendingRecords.size());
            for (WxMassTaskRecord record : pendingRecords) {
                // 检查任务是否被取消或暂停
                WxMassTask currentTask = wxMassTaskMapper.selectById(taskId);
                if (currentTask == null || STATUS_CANCELLED == currentTask.getStatus()
                        || STATUS_PAUSED == currentTask.getStatus()) {
                    log.info("群发任务已暂停或取消，停止执行：taskId={}", taskId);
                    return;
                }

                // 等待到允许的发送时间窗口
                waitForSendWindow(task);

                // 执行发送
                boolean success = sendMessage(task, record);
                if (success) {
                    record.setStatus(RECORD_STATUS_SUCCESS);
                    record.setSendTime(LocalDateTime.now());
                    task.setSuccessCount(task.getSuccessCount() + 1);
                } else {
                    record.setStatus(RECORD_STATUS_FAILED);
                    record.setSendTime(LocalDateTime.now());
                    task.setFailCount(task.getFailCount() + 1);
                }
                wxMassTaskRecordMapper.updateById(record);

                // 更新任务统计
                WxMassTask statUpdate = new WxMassTask();
                statUpdate.setId(taskId);
                statUpdate.setSuccessCount(task.getSuccessCount());
                statUpdate.setFailCount(task.getFailCount());
                wxMassTaskMapper.updateById(statUpdate);

                // 间隔等待
                if (task.getIntervalSeconds() != null && task.getIntervalSeconds() > 0) {
                    sleepSeconds(task.getIntervalSeconds());
                }
            }

            finishTask(taskId);
        } catch (Exception e) {
            log.error("执行群发任务异常：taskId={}", taskId, e);
            WxMassTask updateTask = new WxMassTask();
            updateTask.setId(taskId);
            updateTask.setStatus(STATUS_FAILED);
            wxMassTaskMapper.updateById(updateTask);
        } finally {
            UserContext.clear();
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    public void pauseTask(Long taskId) {
        Long userId = UserContext.getUserId();
        WxMassTask task = getAndValidateTask(taskId, userId);
        if (task.getStatus() != STATUS_RUNNING && task.getStatus() != STATUS_PENDING) {
            throw new BusinessException("只能暂停待执行或执行中的任务");
        }
        WxMassTask updateTask = new WxMassTask();
        updateTask.setId(taskId);
        updateTask.setStatus(STATUS_PAUSED);
        wxMassTaskMapper.updateById(updateTask);

        // 从延迟队列中移除
        redisTemplate.opsForZSet().remove(REDIS_DELAY_QUEUE_KEY, String.valueOf(taskId));
        log.info("群发任务已暂停：taskId={}", taskId);
    }

    @Override
    public void cancelTask(Long taskId) {
        Long userId = UserContext.getUserId();
        WxMassTask task = getAndValidateTask(taskId, userId);
        if (task.getStatus() != STATUS_RUNNING && task.getStatus() != STATUS_PENDING) {
            throw new BusinessException("只能取消待执行或执行中的任务");
        }
        WxMassTask updateTask = new WxMassTask();
        updateTask.setId(taskId);
        updateTask.setStatus(STATUS_CANCELLED);
        wxMassTaskMapper.updateById(updateTask);

        // 从延迟队列中移除
        redisTemplate.opsForZSet().remove(REDIS_DELAY_QUEUE_KEY, String.valueOf(taskId));
        log.info("群发任务已取消：taskId={}", taskId);
    }

    @Override
    public PageResult<WxMassTask> pageTasks(PageQuery query) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        LambdaQueryWrapper<WxMassTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WxMassTask::getUserId, userId);
        if (StrUtil.isNotBlank(query.getKeyword())) {
            wrapper.like(WxMassTask::getName, query.getKeyword());
        }
        wrapper.orderByDesc(WxMassTask::getCreateTime);

        IPage<WxMassTask> page = new Page<>(query.getPageNum(), query.getPageSize());
        wxMassTaskMapper.selectPage(page, wrapper);
        return PageResult.build(page);
    }

    @Override
    public WxMassTask getTaskDetail(Long taskId) {
        Long userId = UserContext.getUserId();
        return getAndValidateTask(taskId, userId);
    }

    @Override
    public void deleteTask(Long taskId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        int rows = wxMassTaskMapper.physicalDeleteById(taskId, userId);
        if (rows == 0) {
            throw new BusinessException("任务不存在或无权删除");
        }

        // 删除关联记录
        LambdaQueryWrapper<WxMassTaskRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WxMassTaskRecord::getTaskId, taskId);
        wxMassTaskRecordMapper.delete(wrapper);

        // 从延迟队列中移除
        redisTemplate.opsForZSet().remove(REDIS_DELAY_QUEUE_KEY, String.valueOf(taskId));
        log.info("群发任务已删除：taskId={}", taskId);
    }

    @Override
    public PageResult<WxMassTaskRecord> pageTaskRecords(Long taskId, PageQuery query) {
        Long userId = UserContext.getUserId();
        // 校验任务访问权限
        getAndValidateTask(taskId, userId);

        LambdaQueryWrapper<WxMassTaskRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WxMassTaskRecord::getTaskId, taskId);
        if (StrUtil.isNotBlank(query.getKeyword())) {
            wrapper.and(w -> w.like(WxMassTaskRecord::getContactWxid, query.getKeyword())
                    .or()
                    .like(WxMassTaskRecord::getNickName, query.getKeyword()));
        }
        wrapper.orderByAsc(WxMassTaskRecord::getId);

        IPage<WxMassTaskRecord> page = new Page<>(query.getPageNum(), query.getPageSize());
        wxMassTaskRecordMapper.selectPage(page, wrapper);
        return PageResult.build(page);
    }

    /**
     * 完成或取消后，将任务状态置为已完成
     */
    private void finishTask(Long taskId) {
        WxMassTask updateTask = new WxMassTask();
        updateTask.setId(taskId);
        updateTask.setStatus(STATUS_COMPLETED);
        wxMassTaskMapper.updateById(updateTask);
        log.info("群发任务执行完成：taskId={}", taskId);
    }

    /**
     * 等待进入允许的发送时间窗口
     */
    private void waitForSendWindow(WxMassTask task) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = task.getStartDate();
        LocalDate endDate = task.getEndDate();

        // 如果早于开始日期，等待到开始日期 00:00
        if (startDate != null && today.isBefore(startDate)) {
            long waitSeconds = java.time.Duration.between(
                    LocalDateTime.now(),
                    startDate.atStartOfDay()
            ).getSeconds();
            if (waitSeconds > 0) {
                log.info("群发任务未到达开始日期，等待 {} 秒", waitSeconds);
                sleepSeconds((int) waitSeconds);
            }
        }

        // 如果晚于结束日期，直接结束
        if (endDate != null && today.isAfter(endDate)) {
            throw new RuntimeException("已超过发送日期区间，任务结束");
        }

        LocalTime now = LocalTime.now();
        LocalTime startTime = task.getStartTime();
        LocalTime endTime = task.getEndTime();

        // 不在发送时段内则等待
        if (startTime != null && endTime != null) {
            if (now.isBefore(startTime)) {
                long waitSeconds = java.time.Duration.between(
                        LocalDateTime.now(),
                        LocalDateTime.of(today, startTime)
                ).getSeconds();
                if (waitSeconds > 0) {
                    log.info("群发任务未到达发送时段，等待 {} 秒", waitSeconds);
                    sleepSeconds((int) waitSeconds);
                }
            } else if (now.isAfter(endTime)) {
                // 已过今天结束时间，等待到明天开始时间
                long waitSeconds = java.time.Duration.between(
                        LocalDateTime.now(),
                        LocalDateTime.of(today.plusDays(1), startTime)
                ).getSeconds();
                if (waitSeconds > 0) {
                    log.info("群发任务已过今天发送时段，等待到明天开始时段：{} 秒", waitSeconds);
                    sleepSeconds((int) waitSeconds);
                }
            }
        }
    }

    /**
     * 调用微信消息发送接口
     */
    private boolean sendMessage(WxMassTask task, WxMassTaskRecord record) {
        try {
            Integer msgType = task.getMsgType();
            if (msgType == null) {
                msgType = MATERIAL_TYPE_TEXT;
            }
            switch (msgType) {
                case MATERIAL_TYPE_IMAGE:
                    return sendImageMessage(task, record);
                case MATERIAL_TYPE_VIDEO:
                    return sendVideoMessage(task, record);
                case MATERIAL_TYPE_FILE:
                    return sendFileMessage(task, record);
                case MATERIAL_TYPE_TEXT:
                case MATERIAL_TYPE_LINK:
                    return sendTextMessage(task, record);
                case MATERIAL_TYPE_MINI_APP:
                    throw new BusinessException("小程序素材暂不支持群发");
                default:
                    return sendTextMessage(task, record);
            }
        } catch (Exception e) {
            log.error("群发消息发送失败：taskId={}，contactWxid={}", task.getId(), record.getContactWxid(), e);
            record.setErrorMsg(StrUtil.blankToDefault(e.getMessage(), "发送失败"));
            return false;
        }
    }

    /**
     * 发送文本/链接消息
     */
    private boolean sendTextMessage(WxMassTask task, WxMassTaskRecord record) {
        String content = task.getContent();
        if (MATERIAL_TYPE_LINK == task.getMsgType()) {
            content = parseLinkContent(content);
        } else {
            content = parseTextContent(content);
        }
        WxSendTextDTO sendTextDTO = new WxSendTextDTO();
        sendTextDTO.setAppId(task.getAppId());
        sendTextDTO.setToWxid(record.getContactWxid());
        sendTextDTO.setContent(content);
        wxMessageService.sendTextMessage(sendTextDTO);
        return true;
    }

    /**
     * 发送图片消息
     */
    private boolean sendImageMessage(WxMassTask task, WxMassTaskRecord record) {
        String imgUrl = parseMediaUrl(task.getContent(), "imgUrl", "imageUrl", "url");
        WxSendImageDTO dto = new WxSendImageDTO();
        dto.setAppId(task.getAppId());
        dto.setToWxid(record.getContactWxid());
        dto.setImgUrl(imgUrl);
        wxMessageService.sendImageMessage(dto);
        return true;
    }

    /**
     * 发送视频消息
     */
    private boolean sendVideoMessage(WxMassTask task, WxMassTaskRecord record) {
        JSONObject json = parseJsonObject(task.getContent());
        String videoUrl = parseMediaUrl(task.getContent(), "videoUrl", "url");
        String thumbUrl = json != null ? json.getString("thumbUrl") : null;
        Integer videoDuration = json != null ? json.getInteger("videoDuration") : null;
        if (videoDuration == null) {
            videoDuration = 0;
        }
        WxSendVideoDTO dto = new WxSendVideoDTO();
        dto.setAppId(task.getAppId());
        dto.setToWxid(record.getContactWxid());
        dto.setVideoUrl(videoUrl);
        dto.setThumbUrl(thumbUrl);
        dto.setVideoDuration(videoDuration);
        wxMessageService.sendVideoMessage(dto);
        return true;
    }

    /**
     * 发送文件消息
     */
    private boolean sendFileMessage(WxMassTask task, WxMassTaskRecord record) {
        JSONObject json = parseJsonObject(task.getContent());
        String fileUrl = parseMediaUrl(task.getContent(), "fileUrl", "url");
        String fileName = json != null ? json.getString("fileName") : null;
        if (StrUtil.isBlank(fileName)) {
            fileName = "文件";
        }
        WxSendFileDTO dto = new WxSendFileDTO();
        dto.setAppId(task.getAppId());
        dto.setToWxid(record.getContactWxid());
        dto.setFileName(fileName);
        dto.setFileUrl(fileUrl);
        wxMessageService.sendFileMessage(dto);
        return true;
    }

    /**
     * 从 JSON 内容中解析文本
     */
    private String parseTextContent(String content) {
        JSONObject json = parseJsonObject(content);
        if (json != null) {
            String text = json.getString("text");
            if (StrUtil.isNotBlank(text)) {
                return text;
            }
        }
        return content;
    }

    /**
     * 从 JSON 内容中解析链接文本
     */
    private String parseLinkContent(String content) {
        JSONObject json = parseJsonObject(content);
        if (json == null) {
            return content;
        }
        String title = json.getString("title");
        String url = json.getString("url");
        String desc = json.getString("desc");
        StringBuilder sb = new StringBuilder();
        if (StrUtil.isNotBlank(title)) {
            sb.append(title);
        }
        if (StrUtil.isNotBlank(desc)) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(desc);
        }
        if (StrUtil.isNotBlank(url)) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(url);
        }
        return sb.length() > 0 ? sb.toString() : content;
    }

    /**
     * 从 JSON 内容中解析媒体 URL
     */
    private String parseMediaUrl(String content, String... keys) {
        JSONObject json = parseJsonObject(content);
        if (json == null) {
            throw new BusinessException("素材内容不是合法的 JSON");
        }
        for (String key : keys) {
            String value = json.getString(key);
            if (StrUtil.isNotBlank(value)) {
                return value;
            }
        }
        throw new BusinessException("素材内容中缺少媒体 URL 字段");
    }

    /**
     * 解析 JSON 内容
     */
    private JSONObject parseJsonObject(String content) {
        if (StrUtil.isBlank(content)) {
            return null;
        }
        try {
            return JSON.parseObject(content);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 校验并解析素材内容
     */
    private MaterialContent resolveMaterial(Long userId, Long materialId, String content) {
        if (materialId == null) {
            if (StrUtil.isBlank(content)) {
                throw new BusinessException("消息内容不能为空");
            }
            return new MaterialContent(MATERIAL_TYPE_TEXT, content);
        }

        Material material = materialMapper.selectById(materialId);
        if (material == null || CommonConstant.DELETED_YES.equals(material.getDeleted())) {
            throw new BusinessException("所选素材不存在");
        }
        if (!material.getUserId().equals(userId)) {
            throw new BusinessException("无权使用他人素材");
        }
        return new MaterialContent(material.getType(), material.getContent());
    }

    /**
     * 素材内容包装类
     */
    private static class MaterialContent {
        private final Integer msgType;
        private final String content;

        MaterialContent(Integer msgType, String content) {
            this.msgType = msgType;
            this.content = content;
        }

        Integer getMsgType() {
            return msgType;
        }

        String getContent() {
            return content;
        }
    }

    /**
     * 校验微信账号归属
     */
    private WxAccount validateAccount(Long userId, String appId) {
        LambdaQueryWrapper<WxAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WxAccount::getUserId, userId)
                .eq(WxAccount::getAppId, appId)
                .eq(WxAccount::getDeleted, 0)
                .last("LIMIT 1");
        WxAccount account = wxAccountMapper.selectOne(wrapper);
        if (account == null) {
            throw new BusinessException("微信账号不存在或无权操作");
        }
        return account;
    }

    /**
     * 目标类型：混合
     */
    private static final int TARGET_TYPE_MIXED = 3;

    /**
     * 校验联系人是否属于当前微信账号
     */
    private List<WxContact> validateContacts(Long userId, String ownerWxid, Integer targetType, List<String> contactWxids) {
        LambdaQueryWrapper<WxContact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WxContact::getUserId, userId)
                .eq(WxContact::getOwnerWxid, ownerWxid)
                .in(WxContact::getContactWxid, contactWxids);
        if (targetType != null && TARGET_TYPE_MIXED != targetType) {
            wrapper.eq(WxContact::getType, targetType);
        }
        List<WxContact> contacts = wxContactMapper.selectList(wrapper);
        if (contacts.isEmpty()) {
            throw new BusinessException("未找到可用的联系人");
        }
        Set<String> existWxids = contacts.stream()
                .map(WxContact::getContactWxid)
                .collect(Collectors.toSet());
        List<String> invalidWxids = contactWxids.stream()
                .filter(wxid -> !existWxids.contains(wxid))
                .collect(Collectors.toList());
        if (!invalidWxids.isEmpty()) {
            throw new BusinessException("以下联系人不在当前微信账号下：" + String.join(",", invalidWxids));
        }
        return contacts;
    }

    /**
     * 校验并查询任务
     */
    private WxMassTask getAndValidateTask(Long taskId, Long userId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        LambdaQueryWrapper<WxMassTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WxMassTask::getId, taskId);
        wrapper.eq(WxMassTask::getUserId, userId);
        WxMassTask task = wxMassTaskMapper.selectOne(wrapper);
        if (task == null) {
            throw new BusinessException("任务不存在或无权访问");
        }
        return task;
    }

    /**
     * 校验发送时段
     */
    private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            return;
        }
        if (!startTime.isBefore(endTime)) {
            throw new BusinessException("发送时段开始时间必须早于结束时间");
        }
    }

    /**
     * 休眠指定秒数
     */
    private void sleepSeconds(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("任务执行被中断", e);
        }
    }
}
