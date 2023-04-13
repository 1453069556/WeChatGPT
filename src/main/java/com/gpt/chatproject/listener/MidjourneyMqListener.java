package com.gpt.chatproject.listener;

import com.gpt.chatproject.constant.MidjourneyConstant;
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
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

@Component
public class MidjourneyMqListener {
    @Autowired
    private WeChatUtils weChatUtils;
    @Autowired
    private FileUtils fileUtils;
    @Value("${queue.check_count}")
    private Integer CHECK_COUNT;
    @Value("${queue.check_delay}")
    private Integer CHECK_DELAY;

    /**
     * 消费消息列表
     *
     * @param mqVo mqVo
     */
    @RabbitListener(queues = "${queue.command.name}", containerFactory = "customContainerFactory")
    public void receiveCommand(MidjourneyMqVo mqVo) throws Exception {
        long messageId = mqVo.getMessageId();
        String fromUser = mqVo.getFromUser();
        int currentCheckCount = 0;
        while (currentCheckCount < CHECK_COUNT) {
            if (!StringUtils.isNotBlank(MidjourneyConstant.getMessages())) {
                // 延时后跳出本次for循环
                TimeUnit.SECONDS.sleep(10);
            }
            // 指令处理过程
            DiscordMessageVo messageVo = MidjourneyUtils.getMessageByMessageId(MidjourneyConstant.getMessages(), messageId);
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
                File file = fileUtils.downloadImageAsync(attachmentsDTO.getUrl());
                // 处理文件下载和存储后的操作
                try {
                    String mediaId = weChatUtils.uploadImageAndGetMediaId(file);
                    weChatUtils.sendKefuImageMessage(fromUser, mediaId);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    Files.deleteIfExists(file.toPath());
                }
                return;
            }
            // 延时后进入下次for循环
            TimeUnit.SECONDS.sleep(CHECK_DELAY);
            currentCheckCount++;
        }
        weChatUtils.sendKefuTextMessage(fromUser, "绘图超时，请稍后再试");
    }
}
