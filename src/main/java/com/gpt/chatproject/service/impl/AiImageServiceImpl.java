package com.gpt.chatproject.service.impl;

import com.gpt.chatproject.enums.DallResponseType;
import com.gpt.chatproject.enums.DallSizeType;
import com.gpt.chatproject.enums.RedisKeyEnum;
import com.gpt.chatproject.service.AiImageService;
import com.gpt.chatproject.utils.*;
import com.gpt.chatproject.vo.MidjourneyVariationVo;
import com.theokanning.openai.image.Image;
import lombok.extern.log4j.Log4j2;
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
    @Value("${queue.command.max_command_length}")
    private Integer MAX_COMMAND_LENGTH;
    @Value("${dall.n}")
    private Integer N;

    @Override
    public void imageMidjourneyMqVariation(WxMpXmlMessage wxImageMessage) throws Exception {
        File imageFile = null;
        try {
            String fromUser = wxImageMessage.getFromUser();
            if (redisUtils.countIncr(RedisKeyEnum.MQ_QUEUE_COUNT, MAX_COMMAND_LENGTH)) {
                MidjourneyVariationVo midjourneyVariationVo = redisUtils.getMidjourneyVariationCatch(fromUser);
                if (ObjectUtils.isEmpty(midjourneyVariationVo)) {
                    midjourneyVariationVo = new MidjourneyVariationVo(fromUser);
                }
                String fromMediaId = wxImageMessage.getMediaId();
                // 如果传入的是图片
                if (StringUtils.isNotBlank(fromMediaId)) {
                    imageFile = weChatUtils.getFileByMediaId(fromMediaId);
                    String url = fileUtils.uploadAndGetUrl(imageFile);
                    midjourneyVariationVo.setUrl(url);
                    redisUtils.updateMidjourneyVariationCatch(fromUser, midjourneyVariationVo);
                    weChatUtils.sendKefuTextMessage(fromUser, "小C已收到您的图片(一分钟内有效)，请在一分钟传入prompt(切记加上/image前缀，否则无效噢)~");
                }
                // 如果传入的是prompt
                String contentPrompt = wxImageMessage.getContent();
                if (StringUtils.isNotBlank(contentPrompt)) {
                    midjourneyVariationVo.setPrompt(contentPrompt.replaceFirst("/image", ""));
                    redisUtils.updateMidjourneyVariationCatch(fromUser, midjourneyVariationVo);
                }
                // 更新检查是否齐全
                if (midjourneyVariationVoIsAlready(midjourneyVariationVo)) {
                    String prompt = midjourneyVariationVo.getUrl() + " " + midjourneyVariationVo.getPrompt();
                    mqUtils.addMqTask(fromUser, prompt);
                    weChatUtils.sendKefuTextMessage(fromUser, "小C已接收到您的图片以及prompt,正在绘图请稍后~");
                }
            } else {
                weChatUtils.sendKefuTextMessage(fromUser, "当前功能过于火爆请稍后再试~");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (imageFile != null && imageFile.exists()) {
                Files.deleteIfExists(imageFile.toPath());
            }
            redisUtils.releaseChatLock(wxImageMessage.getFromUser());
        }
    }

    /**
     * 判断midjourney图生图参数是否齐全
     *
     * @param midjourneyVariationVo midjourneyVariationVo
     * @return
     */
    public boolean midjourneyVariationVoIsAlready(MidjourneyVariationVo midjourneyVariationVo) {
        return StringUtils.isNotBlank(midjourneyVariationVo.getUrl()) && StringUtils.isNotBlank(midjourneyVariationVo.getPrompt());
    }

    @Override
    public void imageMidjourneyMqVoCreate(WxMpXmlMessage wxImageMessage) {
        String fromUser = wxImageMessage.getFromUser();
        try {
            if (redisUtils.countIncr(RedisKeyEnum.MQ_QUEUE_COUNT, MAX_COMMAND_LENGTH)) {
                weChatUtils.sendKefuTextMessage(fromUser, "小C已接收到您的prompt,正在绘图请稍后...");
                String prompt = wxImageMessage.getContent().replaceFirst("/image", "");
                mqUtils.addMqTask(wxImageMessage.getFromUser(), prompt);
            } else {
                weChatUtils.sendKefuTextMessage(fromUser, "当前功能过于火爆请稍后再试~");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            redisUtils.releaseChatLock(wxImageMessage.getFromUser());
        }
    }

    @Override
    public void imageDallVariation(WxMpXmlMessage wxImageMessage) throws IOException {
        File pngImage = null;
        File fileByBase64 = null;
        String fromUser = wxImageMessage.getFromUser();
        try {
            weChatUtils.sendKefuTextMessage(fromUser, "小C已接收到您的图片,正在绘图请稍后...");
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
            e.printStackTrace();
        } finally {
            redisUtils.releaseChatLock(wxImageMessage.getFromUser());
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
            weChatUtils.sendKefuTextMessage(fromUser, "小C已接收到您的prompt,正在绘图请稍后...");
            List<Image> images = defaultDallCreate(wxImageMessage.getContent().replaceFirst("/image", ""));
            for (Image image : images) {
                fileByBase64 = fileUtils.getFileByBase64(image.getB64Json(), ".jpg");
                String uploadMediaId = weChatUtils.uploadImageAndGetMediaId(fileByBase64);
                weChatUtils.sendKefuImageMessage(fromUser, uploadMediaId);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            redisUtils.releaseChatLock(wxImageMessage.getFromUser());
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
