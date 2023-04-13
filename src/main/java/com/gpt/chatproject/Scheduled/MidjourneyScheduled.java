package com.gpt.chatproject.Scheduled;

import com.gpt.chatproject.constant.MidjourneyConstant;
import com.gpt.chatproject.utils.MidjourneyUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@EnableScheduling
@Component
public class MidjourneyScheduled {
    @Value("${midjourney.authorization}")
    private String AUTHORIZATION;
    @Value("${midjourney.channelId}")
    private String CHANNEL_ID;
    @Value("${midjourney.messages_limit}")
    private Integer MESSAGES_LIMIT;

    /**
     * 定时更新消息列表
     */
    @Scheduled(fixedDelay = 15000)
    @Async
    public void checkMessages() {
        MidjourneyConstant.setMessages(MidjourneyUtils.getMessages(AUTHORIZATION, CHANNEL_ID, MESSAGES_LIMIT));
    }
}
