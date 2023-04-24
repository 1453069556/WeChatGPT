package com.gpt.chatproject.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.gpt.chatproject.form.Wechat.WechatResponseTextMessage;
import com.gpt.chatproject.vo.WxRedisCatchVo;
import com.theokanning.openai.completion.chat.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.result.WxMediaUploadResult;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpQrcodeService;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.kefu.WxMpKefuMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class WeChatUtils {
    @Autowired
    private WxMpService wxService;
    @Autowired
    private GptUtils gptUtils;
    @Autowired
    private WxMpService wxMpService;
    @Autowired
    private XmlMapper xmlMapper;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private FileUtils fileUtils;
    @Autowired
    @Qualifier("myThreadPoolTaskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Value("${wxchat.chat_frequency_response}")
    private String CHAT_FREQUENCY_RESPONSE;

    @Value("${wxchat.time_frequency_response}")
    private String TIME_FREQUENCY_RESPONSE;

    @Value("${wxchat.max_replay_tokens}")
    private Integer MAX_REPLAY_TOKENS;
    @Value("${wxchat.server_error_replay}")
    private String SERVER_ERROR_REPLAY;
    @Value("${aliyun.access_key_id}")
    private String ACCESS_KEY_ID;
    @Value("${aliyun.access_key_secret}")
    private String ACCESS_KEY_SECRET;
    @Value("${aliyun.wechat.end_point}")
    private String END_POINT;

    /**
     * 根据fromUser获取渠道二维码
     *
     * @param fromUser
     * @return
     * @throws WxErrorException
     */
    public File getQrcode(String fromUser) throws WxErrorException {
        // 二维码服务
        WxMpQrcodeService qrcodeService = wxService.getQrcodeService();
        // 获取永久二维码ticket
        WxMpQrCodeTicket wxMpQrCodeTicket = qrcodeService.qrCodeCreateLastTicket(fromUser);
        // 返回图片二维码文件
        return qrcodeService.qrCodePicture(wxMpQrCodeTicket);
    }


    /**
     * 用阿里云下载然后上传图片并获取media_id
     *
     * @param alyDataName
     * @return
     * @throws Exception
     */
    public String uploadImageAndGetMediaId(String alyDataName) throws Exception {
        String accessKeyId = ACCESS_KEY_ID;
        String accessKeySecret = ACCESS_KEY_SECRET;
        String endpoint = END_POINT;
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            // 调用ossClient.getObject返回一个OSSObject实例，该实例包含文件内容及文件元信息。
            OSSObject ossObject = ossClient.getObject("wechat-gpt", alyDataName);
            InputStream data = ossObject.getObjectContent();
            // 上传图片并获取media_id
            WxMediaUploadResult wxMediaUploadResult = wxMpService.getMaterialService().mediaUpload(WxConsts.XmlMsgType.IMAGE, "jpg", data);
            return wxMediaUploadResult.getMediaId();
        } catch (Exception e) {
            e.printStackTrace();
            log.debug(e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return null;
    }

    /**
     * 上传图片文件并获取media_id
     *
     * @param image image
     * @return
     * @throws Exception
     */
    public String uploadImageAndGetMediaId(File image) throws Exception {
        // 上传图片并获取media_id
        WxMediaUploadResult wxMediaUploadResult = wxMpService.getMaterialService().mediaUpload(WxConsts.XmlMsgType.IMAGE, image);
        Files.deleteIfExists(image.toPath());
        return wxMediaUploadResult.getMediaId();
    }

    /**
     * 根据mediaId获取文件
     *
     * @param mediaId
     * @return
     * @throws WxErrorException
     */
    public File getFileByMediaId(String mediaId) throws WxErrorException {
        return wxService.getMaterialService().mediaDownload(mediaId);
    }

    /**
     * 获取各种锁
     *
     * @param fromUser
     * @param wxMpXmlMessage
     * @return
     * @throws JsonProcessingException
     */
    public String getLock(WxRedisCatchVo catchVo, String fromUser, WxMpXmlMessage wxMpXmlMessage, long delta) throws IOException {
        String result;
        // 加锁&&一问一答限制
        if (!redisUtils.tryChatLock(fromUser)) {
            result = xmlMapper.writeValueAsString(new WechatResponseTextMessage(fromUser, wxMpXmlMessage.getToUser(), WxConsts.XmlMsgType.TEXT, CHAT_FREQUENCY_RESPONSE));
            return result;
        }
        if (catchVo.getMemberLevel() == null) {
            if (!redisUtils.tryTimeLock(fromUser, delta)) {
                // 过滤每小时会话频率，超过阈值则强制休息一小时
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                ZonedDateTime localDateTime = dateAddSeconds(redisUtils.getExpireByKey(fromUser));
                String replay = TIME_FREQUENCY_RESPONSE + "预计" + localDateTime.format(formatter) + "可以重新开始对话。";
                // 返回提示语
                result = xmlMapper.writeValueAsString(new WechatResponseTextMessage(fromUser, wxMpXmlMessage.getToUser(), WxConsts.XmlMsgType.TEXT, replay));
                sendQrcodeAndReleaseLock(fromUser);
                return result;
            }
        }
        return "";
    }

    private void sendQrcodeAndReleaseLock(String fromUser) {
        threadPoolTaskExecutor.execute(() -> {
            File qrcode = null;
            try {
                qrcode = getQrcode(fromUser);
                WxMediaUploadResult wxMediaUploadResult = wxMpService.getMaterialService().mediaUpload(WxConsts.XmlMsgType.IMAGE, qrcode);
                sendKefuImageMessage(fromUser, wxMediaUploadResult.getMediaId());
                redisUtils.releaseChatLock(fromUser);
            } catch (WxErrorException e) {
                throw new RuntimeException(e);
            } finally {
                safelyDeleteFile(qrcode);
            }
        });
    }

    private void safelyDeleteFile(File file) {
        if (file != null) {
            try {
                Files.deleteIfExists(file.toPath());
            } catch (IOException e) {
                // 这里可以记录日志，但不要再抛出异常，以避免覆盖之前的异常
                log.info("Error deleting file: " + e.getMessage());
            }
        }
    }


    /**
     * 发送客服文本消息
     *
     * @param toUser  接收方
     * @param content 发送内容
     * @return 是否成功发送
     * @throws WxErrorException
     */
    public boolean sendKefuTextMessage(String toUser, String content) throws WxErrorException {
        WxMpKefuMessage wxMpKefuMessage = WxMpKefuMessage.TEXT().toUser(toUser).content(content).build();
        return wxMpService.getKefuService().sendKefuMessage(wxMpKefuMessage);
    }

    /**
     * 发送客服图片消息
     *
     * @param toUser  接收方
     * @param mediaId 图片mediaId
     * @return 是否成功发送
     * @throws WxErrorException
     */
    public boolean sendKefuImageMessage(String toUser, String mediaId) throws WxErrorException {
        WxMpKefuMessage wxMpKefuMessage = WxMpKefuMessage.IMAGE().toUser(toUser).mediaId(mediaId).build();
        return wxMpService.getKefuService().sendKefuMessage(wxMpKefuMessage);
    }

    /**
     * 发送客服图片消息
     *
     * @param toUser 接收方
     * @param url    图片url
     * @return 是否成功发送
     */
    public boolean sendKefuImageMessageByUrl(String toUser, String url) throws IOException, WxErrorException {
        File file = fileUtils.downloadImageAsync(url);
        // 处理文件下载和存储后的操作
        try {
            String mediaId = uploadImageAndGetMediaId(file);
            WxMpKefuMessage wxMpKefuMessage = WxMpKefuMessage.IMAGE().toUser(toUser).mediaId(mediaId).build();
            return wxMpService.getKefuService().sendKefuMessage(wxMpKefuMessage);
        } catch (Exception e) {
            sendKefuTextMessage(toUser, "图片发送失败了,请联系管理员~");
            throw new RuntimeException(e);
        } finally {
            Files.deleteIfExists(file.toPath());
        }
    }

    /**
     * 发送客服图片消息
     *
     * @param toUser 接收方
     * @param url    图片url
     * @return 是否成功发送
     */
    public String getIMediaIdByUrl(String toUser, String url) throws IOException, WxErrorException {
        File file = fileUtils.downloadImageAsync(url);
        // 处理文件下载和存储后的操作
        try {
            return uploadImageAndGetMediaId(file);
        } catch (Exception e) {
            sendKefuTextMessage(toUser, "图片发送失败了,请联系管理员~");
            throw new RuntimeException(e);
        } finally {
            Files.deleteIfExists(file.toPath());
        }
    }

    /**
     * 发送客服消息公用方法
     *
     * @param fromUser
     * @param chatMessage
     * @throws WxErrorException
     */
    public String sendKefuMessages(String fromUser, ChatMessage chatMessage) throws Exception {
        ChatMessage responseMessages = getResponseMessages(chatMessage, fromUser);
        ArrayList<WxMpKefuMessage> kefuMessages = getWxMpKefuMessage(responseMessages.getContent(), fromUser);
        for (WxMpKefuMessage message : kefuMessages) {
            boolean sendResult = wxMpService.getKefuService().sendKefuMessage(message);
            if (sendResult) {
                redisUtils.catchChat(fromUser, responseMessages.getRole(), responseMessages.getContent());
            }
        }
        return responseMessages.getContent();
    }

    /**
     * 获取GPT回复
     *
     * @param actualChatMessage 用户发来的消息
     * @param fromUser          消息来源用户
     * @return
     */
    private ChatMessage getResponseMessages(ChatMessage actualChatMessage, String fromUser) throws Exception {
        WxRedisCatchVo aCatch = redisUtils.getCatch(fromUser);
        ArrayList<ChatMessage> messages = new ArrayList<>();
        // 如果为空说明首次发消息，将此次消息作为第一个缓存
        if (ObjectUtils.isEmpty(aCatch)) {
            messages.add(actualChatMessage);
            return gptUtils.askGpt(messages, aCatch.getChatType());
        }
        // 不为空说明已有缓存，此次请求在其他方法已经加入缓存，无需再次添加
        messages = aCatch.getChatCatch();
        return gptUtils.askGpt(messages, aCatch.getChatType());
    }

    /**
     * 获取WxMpKefuMessage列表
     *
     * @param responseMessages
     * @param fromUserName
     * @return
     */
    private ArrayList<WxMpKefuMessage> getWxMpKefuMessage(String responseMessages, String fromUserName) {
        List<String> contents = MyStringUtils.splitString(responseMessages, MAX_REPLAY_TOKENS);
        ArrayList<WxMpKefuMessage> kefuMessages = new ArrayList<>();
        for (String content : contents) {
            kefuMessages.add(WxMpKefuMessage.TEXT().toUser(fromUserName).content(content).build());
        }
        return kefuMessages;
    }

    /**
     * 服务出错时的友好回复
     *
     * @param fromUserName
     */
    public void serverErrorKefuReplay(String fromUserName) {
        try {
            wxMpService.getKefuService().sendKefuMessage(
                    WxMpKefuMessage.TEXT()
                            .toUser(fromUserName)
                            .content(SERVER_ERROR_REPLAY)
                            .build()
            );
            // 时间频率锁回退1
            redisUtils.timeLockFallback(fromUserName);
        } catch (Exception e) {
            log.debug(e.getMessage());
            e.printStackTrace();
        }
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


    public static ZonedDateTime dateAddSeconds(long seconds) {
        // 创建中国北京的时区对象
        ZoneId chinaZone = ZoneId.of("Asia/Shanghai");
        // 获取当前在中国北京的时间
        ZonedDateTime chinaTime = ZonedDateTime.now(chinaZone);
        // 加上秒数后的时间
        return chinaTime.plus(Duration.ofSeconds(seconds));
    }

}
