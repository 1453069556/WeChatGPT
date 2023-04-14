package com.gpt.chatproject.Scheduled;

import com.gpt.chatproject.config.MidjourneyConfig;
import com.gpt.chatproject.constant.MidjourneyConstant;
import com.gpt.chatproject.utils.MidjourneyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@EnableScheduling
@Component
public class MidjourneyScheduled {
    @Autowired
    private MidjourneyConfig midjourneyConfig;

    /**
     * 定时更新消息列表
     */
    @Scheduled(fixedDelay = 15000)
    @Async
    public void checkMessages() {
        MidjourneyConstant.setMessages(MidjourneyUtils.getMessages(midjourneyConfig.getAuthorization(),
                midjourneyConfig.getChannelId(),
                midjourneyConfig.getMessagesLimit()));
    }
}
