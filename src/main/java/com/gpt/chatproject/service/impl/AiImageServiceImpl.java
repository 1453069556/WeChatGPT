package com.gpt.chatproject.service.impl;

import com.gpt.chatproject.enums.DallResponseType;
import com.gpt.chatproject.enums.DallSizeType;
import com.gpt.chatproject.service.AiImageService;
import com.gpt.chatproject.utils.*;
import com.theokanning.openai.image.Image;
import lombok.extern.log4j.Log4j2;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
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
    private MidjourneyUtils midjourneyUtils;

    @Value("${dall.n}")
    private Integer N;

    @Override
    public void imageMidjourneyMqVariation(WxMpXmlMessage wxImageMessage) throws Exception {
        weChatUtils.sendKefuTextMessage(wxImageMessage.getFromUser(), "暂未开通，敬请期待");
    }

    @Override
    public void imageMidjourneyMqVoCreate(WxMpXmlMessage wxImageMessage) {
        String fromUser = wxImageMessage.getFromUser();
        try {
            weChatUtils.sendKefuTextMessage(fromUser, "小C以接收到您的prompt,正在绘图请稍后...");
            String prompt = wxImageMessage.getContent().replaceFirst("/image", "");
            midjourneyUtils.addMqTask(wxImageMessage.getFromUser(), prompt);
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
            weChatUtils.sendKefuTextMessage(fromUser, "小C以接收到您的图片,正在绘图请稍后...");
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
            weChatUtils.sendKefuTextMessage(fromUser, "小C以接收到您的prompt,正在绘图请稍后...");
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
