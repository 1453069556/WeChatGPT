package com.gpt.chatproject.utils;

import com.gpt.chatproject.vo.DiscordInteractionVo;
import com.gpt.chatproject.vo.MidjourneyMqVo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Component
public class MqUtils {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Value("${queue.command.name}")
    private String MQ_COMMAND_NAME;
    @Value("${midjourney.authorization}")
    private String AUTHORIZATION;
    @Value("${midjourney.applicationId}")
    private String APPLICATION;
    @Value("${midjourney.guild_id}")
    private String GUILD_ID;
    @Value("${midjourney.channelId}")
    private String CHANNEL_ID;
    public void addMqTask(String fromUser, String prompt) throws IOException {
        long messageId = (long) (Math.random() * 999999999L);
        DiscordInteractionVo command = MidjourneyUtils.getCommand(APPLICATION, GUILD_ID, CHANNEL_ID, prompt, messageId);
        MidjourneyUtils.sendCommand(AUTHORIZATION, command);
        rabbitTemplate.convertAndSend(MQ_COMMAND_NAME, new MidjourneyMqVo(fromUser, messageId));
    }
}
