package com.example.admin.job;

import com.example.admin.service.WxMassTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 消息群发任务 Redis 延迟队列监听器
 * <p>
 * 每秒扫描 Redis 延迟队列，取出到期的定时群发任务并触发执行。
 *
 * @author example
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MassTaskDelayJob {

    /**
     * Redis 延迟队列 Key
     */
    private static final String REDIS_DELAY_QUEUE_KEY = "wx:mass:task:delay";

    /**
     * 分布式锁 Key
     */
    private static final String REDIS_DELAY_LOCK_KEY = "wx:mass:task:delay:lock";

    private final StringRedisTemplate redisTemplate;

    private final WxMassTaskService wxMassTaskService;

    /**
     * 每秒扫描一次延迟队列
     */
    @Scheduled(fixedRate = 1000)
    public void scanDelayQueue() {
        // 分布式锁，避免多实例重复消费
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(REDIS_DELAY_LOCK_KEY, "1", 5, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) {
            return;
        }

        try {
            long now = Instant.now().getEpochSecond();
            // 取出 score <= 当前时间的任务
            Set<String> taskIds = redisTemplate.opsForZSet()
                    .rangeByScore(REDIS_DELAY_QUEUE_KEY, 0, now);
            if (taskIds == null || taskIds.isEmpty()) {
                return;
            }

            for (String taskId : taskIds) {
                // 移除该任务，防止重复消费
                Long removed = redisTemplate.opsForZSet()
                        .remove(REDIS_DELAY_QUEUE_KEY, taskId);
                if (removed != null && removed > 0) {
                    log.info("Redis 延迟队列触发群发任务：taskId={}", taskId);
                    try {
                        wxMassTaskService.executeTask(Long.valueOf(taskId));
                    } catch (Exception e) {
                        log.error("Redis 延迟队列执行群发任务失败：taskId={}", taskId, e);
                    }
                }
            }
        } finally {
            redisTemplate.delete(REDIS_DELAY_LOCK_KEY);
        }
    }
}
