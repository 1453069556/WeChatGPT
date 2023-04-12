package com.gpt.chatproject.listener;

import com.gpt.chatproject.utils.FileUtils;
import com.gpt.chatproject.utils.MidjourneyUtils;
import com.gpt.chatproject.utils.MyStringUtils;
import com.gpt.chatproject.utils.WeChatUtils;
import com.gpt.chatproject.vo.DiscordMessageVo;
import com.gpt.chatproject.vo.MidjourneyMqVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

@Component
@EnableScheduling
public class MidjourneyMqListener {
    @Autowired
    private MidjourneyUtils midjourneyUtils;
    @Autowired
    private WeChatUtils weChatUtils;
    @Autowired
    private FileUtils fileUtils;
    @Value("${midjourney.authorization}")
    private String AUTHORIZATION;
    @Value("${midjourney.channelId}")
    private String CHANNEL_ID;
    @Value("${midjourney.messages_limit}")
    private Integer MESSAGES_LIMIT;
    @Value("${midjourney.queue.check_count}")
    private Integer CHECK_COUNT;
    @Value("${midjourney.queue.check_delay}")
    private Integer CHECK_DELAY;
    private String MESSAGES;

    /**
     * 消费消息列表
     *
     * @param mqVo mqVo
     */
    @RabbitListener(queues = "${midjourney.queue.command.name}", containerFactory = "rabbitListenerContainerFactory")
    public void receiveCommand(MidjourneyMqVo mqVo) throws Exception {
        long messageId = mqVo.getMessageId();
        String fromUser = mqVo.getFromUser();
        int currentCheckCount = 0;
        while (currentCheckCount < CHECK_COUNT) {
            if (!StringUtils.isNotBlank(this.MESSAGES)) {
                // 延时后跳出本次for循环
                TimeUnit.SECONDS.sleep(10);
            }
            // 指令处理过程
            DiscordMessageVo messageVo = midjourneyUtils.getMessageByMessageId(this.MESSAGES, messageId);
            if (messageVo == null || messageVo.getAttachments() == null || messageVo.getAttachments().isEmpty()) {
                // 延时后跳出本次for循环
                TimeUnit.SECONDS.sleep(CHECK_DELAY);
                currentCheckCount++;
                continue;
            }
            String percentage = MyStringUtils.matchString("\\((100|[1-9]?[0-9])%\\)", messageVo.getContent());
            if (StringUtils.isNotBlank(percentage)) {
                weChatUtils.sendKefuTextMessage(fromUser, String.format("当前绘制进度%s...", percentage));
            }
            if (!messageVo.getContent().contains("(Waiting to start)") && !StringUtils.isNotBlank(percentage)) {
                DiscordMessageVo.ReferencedMessageDTO.AttachmentsDTO attachmentsDTO = messageVo.getAttachments().get(0);
                File file = null;
                try {
                    file = fileUtils.downloadImage(attachmentsDTO.getUrl());
                    String mediaId = weChatUtils.uploadImageAndGetMediaId(file);
                    weChatUtils.sendKefuImageMessage(fromUser, mediaId);
                } finally {
                    if (file != null) {
                        Files.deleteIfExists(file.toPath());
                    }
                }
                return;
            }
            // 延时后进入下次for循环
            TimeUnit.SECONDS.sleep(CHECK_DELAY);
            currentCheckCount++;
        }
        weChatUtils.sendKefuTextMessage(fromUser, "绘图超时，请稍后再试");
    }

    /**
     * 定时更新消息列表
     */
    @Scheduled(fixedDelay = 8000)
    public void checkMessages() throws IOException {
        this.MESSAGES = midjourneyUtils.getMessages(AUTHORIZATION, CHANNEL_ID, MESSAGES_LIMIT);
    }
}
