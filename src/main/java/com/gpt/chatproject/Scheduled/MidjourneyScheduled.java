package com.gpt.chatproject.Scheduled;

import com.gpt.chatproject.config.MidjourneyConfig;
import com.gpt.chatproject.constant.ConsumerCounterRunning;
import com.gpt.chatproject.constant.MidjourneyConstant;
import com.gpt.chatproject.utils.MidjourneyUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledFuture;

@Component
@Slf4j
public class MidjourneyScheduled {
    @Autowired
    private MidjourneyConfig midjourneyConfig;
    private final String cron;
    private volatile ScheduledFuture<?> future; // 用于保存定时任务的引用
    private final TaskScheduler taskScheduler; // 用于执行定时任务的 TaskScheduler

    // 构造函数注入 TaskScheduler
    @Autowired
    public MidjourneyScheduled(@Value("${queue.check_message_cron}") String cron, TaskScheduler taskScheduler) {
        this.cron = cron;
        this.taskScheduler = taskScheduler;
    }

    /**
     * 启动定时更新消息列表
     */
    public void startCheck() {
        if (future == null) { // 如果定时任务尚未启动
            synchronized (this) { // 使用同步块确保线程安全
                if (future == null) { // 再次检查，以避免多个线程同时创建定时任务
                    // 使用 TaskScheduler 启动定时任务，并将任务引用保存在 future 变量中
                    future = taskScheduler.schedule(() -> {
                        log.info("startCheck任务执行中...");
                        MidjourneyConstant.setMessages(MidjourneyUtils.getMessages(midjourneyConfig.getAuthorization(),
                                midjourneyConfig.getChannelId(),
                                midjourneyConfig.getMessagesLimit()));
                    }, new CronTrigger(cron)); // Cron 表达式
                }
            }
        }
        ConsumerCounterRunning.incrementAndGet(); // 增加消费者计数器的值
    }

    // 停止定时任务的方法
    public void stopCheck() {
        if (ConsumerCounterRunning.decrementAndGet() == 0) { // 减少消费者计数器的值，如果计数器为 0
            synchronized (this) { // 使用同步块确保线程安全
                // 如果 future 不为 null，且任务尚未取消
                if (future != null && !future.isCancelled()) {
                    future.cancel(false); // 取消定时任务
                    future = null; // 将 future 设置为 null，以便下次可以重新启动任务
                }
            }
        }
    }
}
