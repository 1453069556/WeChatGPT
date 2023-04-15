package com.gpt.chatproject.utils;

import com.gpt.chatproject.config.MidjourneyConfig;
import com.gpt.chatproject.constant.ConsumerCounterTotal;
import com.gpt.chatproject.vo.DiscordHttpCustomVo;
import com.gpt.chatproject.vo.DiscordHttpInteractionVo;
import com.gpt.chatproject.vo.MidjourneyMqVo;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MqUtils {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private WeChatUtils weChatUtils;
    @Autowired
    private MidjourneyConfig midConfig;
    @Value("${queue.command.name}")
    private String MQ_COMMAND_NAME;
    @Value("${queue.command.max_command_length}")
    private Integer MAX_COMMAND_LENGTH;

    public void addMidjourneyMqTask(String fromUser, String prompt) throws WxErrorException {
        if (ConsumerCounterTotal.get() < MAX_COMMAND_LENGTH) {
            ConsumerCounterTotal.incrementAndGet();
            long messageId = (long) (Math.random() * 999999999L);
            DiscordHttpInteractionVo command = MidjourneyUtils.getCommandVo(midConfig.getApplicationId(),
                    midConfig.getGuildId(), midConfig.getChannelId(), prompt, messageId);
            MidjourneyUtils.sendCommand(midConfig.getAuthorization(), command);
            rabbitTemplate.convertAndSend(MQ_COMMAND_NAME, new MidjourneyMqVo(fromUser, messageId));
        } else {
            weChatUtils.sendKefuTextMessage(fromUser, "当前功能过于火爆请稍后再试~");
        }
    }

    public void addMidjourneyCustomMqTask(String fromUser, long messageId, String discordMessageId, String custom) throws WxErrorException {
        if (ConsumerCounterTotal.get() < MAX_COMMAND_LENGTH) {
            ConsumerCounterTotal.incrementAndGet();
            DiscordHttpCustomVo command = MidjourneyUtils.getCustomVo(midConfig.getApplicationId(),
                    midConfig.getGuildId(), midConfig.getChannelId(), discordMessageId, custom);
            MidjourneyUtils.sendCustomCommand(midConfig.getAuthorization(), command);
            rabbitTemplate.convertAndSend(MQ_COMMAND_NAME, new MidjourneyMqVo(fromUser, messageId));
        } else {
            weChatUtils.sendKefuTextMessage(fromUser, "当前功能过于火爆请稍后再试~");
        }
    }

}
