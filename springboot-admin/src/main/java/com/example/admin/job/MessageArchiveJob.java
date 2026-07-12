package com.example.admin.job;

import com.example.admin.mapper.WxMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 微信消息归档定时任务
 * <p>
 * 每天凌晨 3 点将 90 天前的消息迁移到 wx_message_history 表，并删除原表记录。
 *
 * @author example
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageArchiveJob {

    /**
     * 保留最近 90 天的消息在热表
     */
    private static final int RETAIN_DAYS = 90;

    private final WxMessageMapper wxMessageMapper;

    /**
     * 每天凌晨 03:00 执行归档
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void archive() {
        try {
            LocalDateTime retainTime = LocalDateTime.now().minusDays(RETAIN_DAYS);
            long beforeTime = retainTime.atZone(ZoneId.systemDefault()).toEpochSecond();

            log.info("开始归档 {} 秒之前（{} 天前）的消息", beforeTime, RETAIN_DAYS);
            int archivedCount = wxMessageMapper.archiveMessages(beforeTime);
            int deletedCount = wxMessageMapper.deleteArchivedMessages(beforeTime);
            log.info("消息归档完成：归档 {} 条，删除原表 {} 条", archivedCount, deletedCount);
        } catch (Exception e) {
            log.error("消息归档任务执行失败", e);
        }
    }
}
