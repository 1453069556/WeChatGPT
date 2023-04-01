package com.gpt.chatproject.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.gpt.chatproject.vo.WechatResponseTextMessage;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.result.WxMediaUploadResult;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpQrcodeService;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.kefu.WxMpKefuMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class WeChatUtils {
    @Autowired
    private WxMpService wxService;

    @Autowired
    private WxMpService wxMpService;
    @Autowired
    private XmlMapper xmlMapper;
    @Autowired
    private RedisUtils redisUtils;
    @Value("${wxchat.chat_frequency_response}")
    private String CHAT_FREQUENCY_RESPONSE;

    @Value("${wxchat.time_frequency_response}")
    private String TIME_FREQUENCY_RESPONSE;

    public File getQrcode(String fromUser) throws WxErrorException {
        // 二维码服务
        WxMpQrcodeService qrcodeService = wxService.getQrcodeService();
        // 获取永久二维码ticket
        WxMpQrCodeTicket wxMpQrCodeTicket = qrcodeService.qrCodeCreateLastTicket(fromUser);
        // 返回图片二维码文件
        return qrcodeService.qrCodePicture(wxMpQrCodeTicket);
    }

    /**
     * 获取各种锁
     *
     * @param fromUser
     * @param wxMpXmlMessage
     * @return
     * @throws JsonProcessingException
     * @throws WxErrorException
     */
    public String getLock(String fromUser, WxMpXmlMessage wxMpXmlMessage) throws JsonProcessingException, WxErrorException {
        String result;
        // 加锁&&一问一答限制
        if (!redisUtils.tryChatLock(fromUser)) {
            result = xmlMapper.writeValueAsString(new WechatResponseTextMessage(fromUser,
                    wxMpXmlMessage.getToUser(), WxConsts.XmlMsgType.TEXT, CHAT_FREQUENCY_RESPONSE));
            return result;
        }
        // 过滤每小时会话频率，超过阈值则强制休息一小时
        if (!redisUtils.tryTimeLock(fromUser)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            LocalDateTime localDateTime = dateAddSeconds(redisUtils.getExpireByKey(fromUser));
            String replay = TIME_FREQUENCY_RESPONSE + "预计" + localDateTime.format(formatter) + "可以重新开始对话。";
            // 返回提示语
            result = xmlMapper.writeValueAsString(new WechatResponseTextMessage(fromUser,
                    wxMpXmlMessage.getToUser(), WxConsts.XmlMsgType.TEXT, replay));
            // 发送专属邀请二维码
            File qrcode = getQrcode(fromUser);
            WxMediaUploadResult wxMediaUploadResult = wxMpService.getMaterialService().mediaUpload(WxConsts.XmlMsgType.IMAGE, qrcode);
            WxMpKefuMessage kefuMessage = WxMpKefuMessage.IMAGE().toUser(fromUser).mediaId(wxMediaUploadResult.getMediaId()).build();
            wxMpService.getKefuService().sendKefuMessage(kefuMessage);
            redisUtils.releaseChatLock(fromUser);
            return result;
        }
        return "";
    }

    /**
     * 秒换算为时分秒
     *
     * @param seconds
     * @return
     */
    public static String formatDuration(String format, long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;
        return String.format(format, hours, minutes, remainingSeconds);
    }


    public static LocalDateTime dateAddSeconds(long seconds) {
        // 当前北京时间
        LocalDateTime now = LocalDateTime.now();
        // 加上秒数后的时间
        return now.plus(Duration.ofSeconds(seconds));
    }

}
