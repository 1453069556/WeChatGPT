package com.gpt.chatproject.service.impl;

import com.gpt.chatproject.constant.ConsumerCounterTotal;
import com.gpt.chatproject.enums.DallResponseType;
import com.gpt.chatproject.enums.DallSizeType;
import com.gpt.chatproject.service.AiImageService;
import com.gpt.chatproject.utils.*;
import com.gpt.chatproject.vo.MidjourneyRedisVo;
import com.theokanning.openai.image.Image;
import lombok.extern.log4j.Log4j2;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Service
@Log4j2
public class AiImageServiceImpl implements AiImageService {
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private WeChatUtils weChatUtils;
    @Autowired
    private DallUtils dallUtils;
    @Autowired
    private FileUtils fileUtils;
    @Autowired
    private MqUtils mqUtils;
    @Value("${dall.n}")
    private Integer N;
    @Value("${wxchat.ai_pic_response}")
    private String AI_PRC_RESPONSE;
    @Value("${wxchat.pic_busy_response}")
    private String PIC_BUSY_RESPONSE;
    @Value("${wxchat.pic_proc_response}")
    private String PIC_PROC_RESPONSE;
    @Value("${queue.command.max_command_length}")
    private Integer MAX_COMMAND_LENGTH;

    @Override
    public void imageMidjourneyVariation(WxMpXmlMessage wxImageMessage) throws Exception {
        File imageFile = null;
        String fromUser = wxImageMessage.getFromUser();
        try {
            if (redisUtils.aiPicIsLock(fromUser)) {
                weChatUtils.sendKefuTextMessage(fromUser, PIC_PROC_RESPONSE);
                return;
            }
            if (ConsumerCounterTotal.get() < MAX_COMMAND_LENGTH) {
                MidjourneyRedisVo midjourneyRedisVo = redisUtils.getMidjourneyRedisCatch(fromUser);
                if (midjourneyRedisVo == null) {
                    midjourneyRedisVo = new MidjourneyRedisVo(fromUser);
                }
                String fromMediaId = wxImageMessage.getMediaId();
                // 如果传入的是图片
                if (StringUtils.isNotBlank(fromMediaId)) {
                    imageFile = weChatUtils.getFileByMediaId(fromMediaId);
                    String url = fileUtils.uploadAndGetUrl(imageFile);
                    midjourneyRedisVo.setUrl(url);
                    redisUtils.midjourneyRedisCatch(midjourneyRedisVo);
                    weChatUtils.sendKefuTextMessage(fromUser,
                            "小C已收到您的图片(5分钟内有效)，请传入对此图片修饰的prompt。\n\n" +
                                    "prompt切记加上前缀\n/modifier \n否则无效噢~");
                }
                // 如果传入的是prompt
                String contentPrompt = wxImageMessage.getContent();
                if (StringUtils.isNotBlank(contentPrompt)) {
                    midjourneyRedisVo.setPrompt(contentPrompt
                            .replaceFirst("/modifier", "")
                            .replaceAll("--", ""));
                    redisUtils.midjourneyRedisCatch(midjourneyRedisVo);
                }
                // 更新检查是否齐全
                if (midjourneyVariationVoIsAlready(midjourneyRedisVo)) {
                    String prompt = midjourneyRedisVo.getUrl() + " " + midjourneyRedisVo.getPrompt();
                    if (redisUtils.tryAiPicLock(fromUser)) {
                        mqUtils.addMidjourneyMqTask(fromUser, prompt);
                    }
                    weChatUtils.sendKefuTextMessage(fromUser, AI_PRC_RESPONSE);
                }
            } else {
                weChatUtils.sendKefuTextMessage(wxImageMessage.getFromUser(), PIC_BUSY_RESPONSE);
            }
        } catch (Exception e) {
            weChatUtils.serverErrorKefuReplay(fromUser);
            redisUtils.releasePicLock(fromUser);
            throw new RuntimeException(e);
        } finally {
            if (imageFile != null && imageFile.exists()) {
                Files.deleteIfExists(imageFile.toPath());
            }
            redisUtils.releaseChatLock(fromUser);
        }
    }

    @Override
    public void imageMidjourneyCustom(WxMpXmlMessage wxMessage) {
        MidjourneyRedisVo midjourneyRedisVo;
        String fromUser = wxMessage.getFromUser();
        try {
            if (redisUtils.aiPicIsLock(fromUser)) {
                weChatUtils.sendKefuTextMessage(fromUser, PIC_PROC_RESPONSE);
                return;
            }
            if (ConsumerCounterTotal.get() < MAX_COMMAND_LENGTH) {
                String custom = wxMessage.getContent();
                midjourneyRedisVo = redisUtils.getMidjourneyRedisCatch(fromUser);
                if (midjourneyRedisVo == null) {
                    weChatUtils.sendKefuTextMessage(fromUser, "指令超时，可以重新绘图噢~");
                    return;
                }
                List<String> customs = midjourneyRedisVo.getCustoms();
                // 如果从列表中清除成功
                if (!ObjectUtils.isEmpty(customs) && customs.contains(custom)) {
                    customs.remove(custom);
                    weChatUtils.sendKefuTextMessage(fromUser, AI_PRC_RESPONSE);
                    if (!ObjectUtils.isEmpty(midjourneyRedisVo)) {
                        redisUtils.midjourneyRedisCatch(midjourneyRedisVo);
                    }
                    if (redisUtils.tryAiPicLock(fromUser)) {
                        mqUtils.addMidjourneyCustomMqTask(fromUser, midjourneyRedisVo.getMessageId(),
                                midjourneyRedisVo.getDiscordMessageId(), custom);
                    }
                } else {
                    weChatUtils.sendKefuTextMessage(fromUser, "您已经发送过此指令或指令有误，请核对~");
                }
            } else {
                weChatUtils.sendKefuTextMessage(wxMessage.getFromUser(), PIC_BUSY_RESPONSE);
            }
        } catch (WxErrorException e) {
            weChatUtils.serverErrorKefuReplay(fromUser);
            redisUtils.releasePicLock(fromUser);
            throw new RuntimeException(e);
        } finally {
            redisUtils.releaseChatLock(fromUser);
        }
    }

    /**
     * 判断midjourney图生图参数是否齐全
     *
     * @param midjourneyRedisVo midjourneyRedisVo
     * @return
     */
    public boolean midjourneyVariationVoIsAlready(MidjourneyRedisVo midjourneyRedisVo) {
        return StringUtils.isNotBlank(midjourneyRedisVo.getUrl()) && StringUtils.isNotBlank(midjourneyRedisVo.getPrompt());
    }

    @Override
    public void imageMidjourneyCreate(WxMpXmlMessage wxImageMessage) throws WxErrorException {
        String fromUser = wxImageMessage.getFromUser();
        try {
            if (redisUtils.aiPicIsLock(fromUser)) {
                weChatUtils.sendKefuTextMessage(fromUser, PIC_PROC_RESPONSE);
                return;
            }
            if (ConsumerCounterTotal.get() < MAX_COMMAND_LENGTH) {
                weChatUtils.sendKefuTextMessage(fromUser, AI_PRC_RESPONSE);
                String prompt = wxImageMessage.getContent()
                        .replaceFirst("/imagine", "")
                        .replaceAll("--", "");
                if (redisUtils.tryAiPicLock(fromUser)) {
                    mqUtils.addMidjourneyMqTask(wxImageMessage.getFromUser(), prompt);
                }
            } else {
                weChatUtils.sendKefuTextMessage(wxImageMessage.getFromUser(), PIC_BUSY_RESPONSE);
            }
        } catch (Exception e) {
            weChatUtils.serverErrorKefuReplay(fromUser);
            redisUtils.releasePicLock(fromUser);
            throw new RuntimeException(e);
        } finally {
            redisUtils.releaseChatLock(fromUser);
        }
    }

    @Override
    public void imageDallVariation(WxMpXmlMessage wxImageMessage) throws IOException, WxErrorException {
        File pngImage = null;
        File fileByBase64 = null;
        String fromUser = wxImageMessage.getFromUser();
        try {
            if (!redisUtils.tryAiPicLock(fromUser)) {
                weChatUtils.sendKefuTextMessage(fromUser, PIC_PROC_RESPONSE);
                return;
            }
            weChatUtils.sendKefuTextMessage(fromUser, AI_PRC_RESPONSE);
            String fromMediaId = wxImageMessage.getMediaId();
            // jpg转png,getFileByMediaId获取到的是jpg
            File imageByMediaId = weChatUtils.getFileByMediaId(fromMediaId);
            pngImage = fileUtils.jpgToPng(FileUtils.scaleImage(imageByMediaId));
            List<Image> images = defaultDallVariation(pngImage);
            for (Image image : images) {
                fileByBase64 = fileUtils.getFileByBase64(image.getB64Json(), ".jpg");
                String uploadMediaId = weChatUtils.uploadImageAndGetMediaId(fileByBase64);
                weChatUtils.sendKefuImageMessage(fromUser, uploadMediaId);
            }
        } catch (Exception e) {
            weChatUtils.serverErrorKefuReplay(fromUser);
            redisUtils.releasePicLock(fromUser);
            e.printStackTrace();
        } finally {
            redisUtils.releasePicLock(fromUser);
            redisUtils.releaseChatLock(fromUser);
            if (pngImage != null) {
                Files.deleteIfExists(pngImage.toPath());
            }
            if (fileByBase64 != null) {
                Files.deleteIfExists(fileByBase64.toPath());
            }
        }
    }

    @Override
    public void imageDallCreate(WxMpXmlMessage wxImageMessage) throws IOException {
        File fileByBase64 = null;
        String fromUser = wxImageMessage.getFromUser();
        try {
            if (!redisUtils.tryAiPicLock(fromUser)) {
                weChatUtils.sendKefuTextMessage(fromUser, PIC_PROC_RESPONSE);
                return;
            }
            weChatUtils.sendKefuTextMessage(fromUser, AI_PRC_RESPONSE);
            List<Image> images = defaultDallCreate(wxImageMessage.getContent().replaceFirst("/imagine", ""));
            for (Image image : images) {
                fileByBase64 = fileUtils.getFileByBase64(image.getB64Json(), ".jpg");
                String uploadMediaId = weChatUtils.uploadImageAndGetMediaId(fileByBase64);
                weChatUtils.sendKefuImageMessage(fromUser, uploadMediaId);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            redisUtils.releasePicLock(fromUser);
            redisUtils.releaseChatLock(fromUser);
            if (fileByBase64 != null) {
                Files.deleteIfExists(fileByBase64.toPath());
            }
        }
    }

    /**
     * dall生图方案
     *
     * @param file file
     * @return List<Image>
     */
    private List<Image> defaultDallVariation(File file) {
        return dallUtils.dall2(file, N, DallSizeType.LARGE, DallResponseType.B64_JSON);
    }

    /**
     * prompt生图方案-dall
     *
     * @param prompt prompt
     * @return List<Image>
     */
    private List<Image> defaultDallCreate(String prompt) {
        return dallUtils.dall2(prompt, N, DallSizeType.LARGE, DallResponseType.B64_JSON);
    }
}
