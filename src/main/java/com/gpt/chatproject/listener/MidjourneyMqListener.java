package com.gpt.chatproject.listener;

import com.gpt.chatproject.Scheduled.MidjourneyScheduled;
import com.gpt.chatproject.constant.MidjourneyConstant;
import com.gpt.chatproject.utils.MidjourneyUtils;
import com.gpt.chatproject.utils.MyStringUtils;
import com.gpt.chatproject.utils.RedisUtils;
import com.gpt.chatproject.utils.WeChatUtils;
import com.gpt.chatproject.vo.DiscordHttpMessageVo;
import com.gpt.chatproject.vo.MidjourneyMqVo;
import com.gpt.chatproject.vo.MidjourneyRedisVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class MidjourneyMqListener {
    @Autowired
    private WeChatUtils weChatUtils;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private MidjourneyScheduled midjourneyScheduled;
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
    public void receiveCommand(MidjourneyMqVo mqVo) {
        midjourneyScheduled.startCheck();
        try {
            long messageId = mqVo.getMessageId();
            String fromUser = mqVo.getFromUser();
            int currentCheckCount = 0;
            // 获取redis缓存
            MidjourneyRedisVo midCatch = redisUtils.getMidjourneyRedisCatch(fromUser);
            while (currentCheckCount < CHECK_COUNT) {
                if (StringUtils.isBlank(MidjourneyConstant.getMessages())) {
                    // 延时后跳出本次for循环
                    TimeUnit.SECONDS.sleep(10);
                }
                // 指令处理过程
                DiscordHttpMessageVo messageVo = MidjourneyUtils.getMessageByMessageId(MidjourneyConstant.getMessages(),
                        messageId, midCatch);
                // 这个是处于提示回馈
                if (messageVo == null || messageVo.getAttachments() == null
                        || messageVo.getAttachments().isEmpty()) {
                    // 延时后跳出本次for循环
                    TimeUnit.SECONDS.sleep(CHECK_DELAY);
                    currentCheckCount++;
                    continue;
                }
                if (messageVo.getEmbeds().size() > 0){
                    weChatUtils.sendKefuTextMessage(fromUser, "小C希望你换指令重试噢~");
                    return;
                }
                String percentage = MyStringUtils.matchString("\\((100|[1-9]?[0-9])%\\)", messageVo.getContent());
                if (StringUtils.isNotBlank(percentage)) {
                    weChatUtils.sendKefuTextMessage(fromUser, String.format("当前绘制进度%s...", percentage));
                }

                if (!messageVo.getContent().contains("(Waiting to start)") && StringUtils.isBlank(percentage)) {
                    DiscordHttpMessageVo.ReferencedMessageDTO.AttachmentsDTO attachmentsDTO = messageVo.getAttachments().get(0);
                    String sendOkMessage = getSendOkMessage(messageVo);
                    String mediaId = weChatUtils.getIMediaIdByUrl(fromUser, attachmentsDTO.getUrl());
                    if (StringUtils.isBlank(mediaId)) {
                        return;
                    }
                    MidjourneyRedisVo midjourneyRedisVo = new MidjourneyRedisVo(fromUser);
                    if (midCatch != null) {
                        BeanUtils.copyProperties(midCatch, midjourneyRedisVo);
                    }
                    // 如果有文件代表需要发送指令列表
                    if (StringUtils.isNotBlank(sendOkMessage)) {
                        weChatUtils.sendKefuTextMessage(fromUser, sendOkMessage);
                        // TODO 这里判断是否需要更新discordId，因为大图类不需要修改id，否则会影响后面的迭代生成
                        midjourneyRedisVo.setDiscordMessageId(messageVo.getId());
                    }
                    weChatUtils.sendKefuImageMessage(fromUser, mediaId);
                    midjourneyRedisVo.setMessageId(messageId);
                    List<String> attachmentsIds = new ArrayList<>();
                    // 判断attachmentsIds缓存是否需重新生成
                    if (midCatch == null || midCatch.getAttachmentsIds() == null) {
                        attachmentsIds.add(messageVo.getAttachments().get(0).getId());
                    } else {
                        attachmentsIds = midCatch.getAttachmentsIds();
                        attachmentsIds.add(messageVo.getAttachments().get(0).getId());
                    }
                    midjourneyRedisVo.setAttachmentsIds(attachmentsIds);
                    // 如果customs为空代表接收到大图，不做指令存储
                    List<String> customs = getCustoms(messageVo);
                    if (customs != null) {
                        midjourneyRedisVo.setCustoms(customs);
                    }
                    redisUtils.midjourneyRedisCatch(midjourneyRedisVo);
                    return;
                }
                // 延时后进入下次for循环
                TimeUnit.SECONDS.sleep(CHECK_DELAY);
                currentCheckCount++;
            }
            weChatUtils.sendKefuTextMessage(fromUser, "绘图超时，请检查指令内容是否违规，如果多次违规将被拉黑，请稍后再试。");
        } catch (Exception e) {
            log.info(e.getMessage());
        } finally {
            // 关闭check
            midjourneyScheduled.stopCheck();
            // 释放绘图锁
            redisUtils.releasePicLock(mqVo.getFromUser());
        }
    }

    /**
     * 通过DiscordMessageVo遍历放入List
     *
     * @param messageVo messageVo
     * @return
     */
    public static List<String> getCustoms(DiscordHttpMessageVo messageVo) {
        List<DiscordHttpMessageVo.ComponentsDTO> components = messageVo.getComponents();
        if (components.get(0).getComponents().size() < 4) {
            return null;
        }
        ArrayList<String> customs = new ArrayList<>();
        for (DiscordHttpMessageVo.ComponentsDTO component : components) {
            for (DiscordHttpMessageVo.ComponentsDTO.ComponentsDTOInner innerComponent : component.getComponents()) {
                customs.add(innerComponent.getCustomId());
            }
        }
        return customs;
    }

    public static String getSendOkMessage(DiscordHttpMessageVo messageVo) {
        StringBuilder hrefButton = new StringBuilder();
        hrefButton.append("可通过点击以下指令操作图片:\n\n");
        List<DiscordHttpMessageVo.ComponentsDTO> components = messageVo.getComponents();
        for (int index = 0; index < components.size(); index++) {
            List<DiscordHttpMessageVo.ComponentsDTO.ComponentsDTOInner> components2 = components.get(index).getComponents();
            if (components2.size() < 4) {
                return null;
            }
            for (int i = 0; i < components2.size(); i++) {
                DiscordHttpMessageVo.ComponentsDTO.ComponentsDTOInner componentsDTOInner = components2.get(i);
                String text;
                switch (i) {
                    case 0:
                        text = index == 0 ? "取左上图" : "左上生成";
                        hrefButton.append(MyStringUtils.generateMidjourneyHrefButton("\uD83D\uDC48☝", componentsDTOInner.getCustomId(), text));
                        break;
                    case 1:
                        text = index == 0 ? "取右上图\n\n" : "右上生成\n\n";
                        hrefButton.append(MyStringUtils.generateMidjourneyHrefButton("\uD83D\uDC49☝", componentsDTOInner.getCustomId(), text));
                        break;
                    case 2:
                        text = index == 0 ? "取左下图" : "左下生成";
                        hrefButton.append(MyStringUtils.generateMidjourneyHrefButton("\uD83D\uDC48\uD83D\uDC47", componentsDTOInner.getCustomId(), text));
                        break;
                    case 3:
                        text = index == 0 ? "取右下图\n\n" : "右下生成";
                        hrefButton.append(MyStringUtils.generateMidjourneyHrefButton("\uD83D\uDC49\uD83D\uDC47", componentsDTOInner.getCustomId(), text));
                        break;
                    case 4:
                        text = "基于原指令重铸一组新图\n\n";
                        hrefButton.append(MyStringUtils.generateMidjourneyHrefButton("✌", componentsDTOInner.getCustomId(), text));
                        break;
                }
            }
        }
        return hrefButton.toString();
    }

}
