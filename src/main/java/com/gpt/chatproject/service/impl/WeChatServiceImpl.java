package com.gpt.chatproject.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.gpt.chatproject.enums.GptRoleType;
import com.gpt.chatproject.service.WeChatService;
import com.gpt.chatproject.utils.GptUtils;
import com.gpt.chatproject.utils.RedisUtils;
import com.gpt.chatproject.vo.WechatResponseTextMessage;
import com.gpt.chatproject.vo.WxRedisCatchVo;
import com.theokanning.openai.completion.chat.ChatMessage;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.result.WxMediaUploadResult;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.kefu.WxMpKefuMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.InputStream;
import java.util.ArrayList;

@Service
public class WeChatServiceImpl implements WeChatService {
    @Autowired
    private GptUtils gptUtils;

    @Autowired
    private WxMpService wxMpService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private XmlMapper xmlMapper;

    @Value("${wxchat.welcome_words}")
    private String WELCOME_WORDS;

    @Value("${wxchat.default_welcome_words_end}")
    private String DEFAULT_WELCOME_WORDS_END;

    @Value("${wxchat.server_error_replay}")
    private String SERVER_ERROR_REPLAY;

    @Value("${wxchat.frequency_response}")
    private String FREQUENCY_RESPONSE;

    @Value("${wxchat.max_tokens}")
    private Integer MAX_TOKENS;

    @Value("${wxchat.chars_overflow_response}")
    private String CHARS_OVERFLOW_RESPONSE;


    @Override
    public String shouldFilterMessage(WxMpXmlMessage wxMpXmlMessage) throws JsonProcessingException {
        String fromUser = wxMpXmlMessage.getFromUser();
        String msgType = wxMpXmlMessage.getMsgType();
        String result;
        // 是文本消息才做以下处理
        if (WxConsts.XmlMsgType.TEXT.equals(msgType)) {
            // 字数限制
            if (wxMpXmlMessage.getContent().length() > MAX_TOKENS) {
                result = xmlMapper.writeValueAsString(new WechatResponseTextMessage(fromUser,
                        wxMpXmlMessage.getToUser(), WxConsts.XmlMsgType.TEXT, CHARS_OVERFLOW_RESPONSE));
                return result;
            }
            // 加锁&&一问一答限制
            if (!redisUtils.tryLock(fromUser)) {
                result = xmlMapper.writeValueAsString(new WechatResponseTextMessage(fromUser,
                        wxMpXmlMessage.getToUser(), WxConsts.XmlMsgType.TEXT, FREQUENCY_RESPONSE));
                return result;
            }
        }
        // 是语音消息才做以下处理
        if (WxConsts.XmlMsgType.VOICE.equals(msgType)) {
            // 字数限制
            if (wxMpXmlMessage.getRecognition().length() > MAX_TOKENS) {
                result = xmlMapper.writeValueAsString(new WechatResponseTextMessage(fromUser,
                        wxMpXmlMessage.getToUser(), WxConsts.XmlMsgType.TEXT, CHARS_OVERFLOW_RESPONSE));
                return result;
            }
            // 加锁&&一问一答限制
            if (!redisUtils.tryLock(fromUser)) {
                result = xmlMapper.writeValueAsString(new WechatResponseTextMessage(fromUser,
                        wxMpXmlMessage.getToUser(), WxConsts.XmlMsgType.TEXT, FREQUENCY_RESPONSE));
                return result;
            }
        }
        return "";
    }

    /**
     * 异步回复文本消息
     *
     * @param wechatTextMessage
     */
    @Override
    public void textEvent(WxMpXmlMessage wechatTextMessage) {
        try {
            String content = wechatTextMessage.getContent();
            ChatMessage actualChatMessage = new ChatMessage(GptRoleType.USER.getRole(), content);
            String fromUser = wechatTextMessage.getFromUser();
            redisUtils.catchChat(fromUser, GptRoleType.USER.getRole(), content);
            ChatMessage responseMessages = getResponseMessages(actualChatMessage, fromUser);
            WxMpKefuMessage kefuMessage = getWxMpKefuMessage(responseMessages.getContent(), fromUser);
            boolean sendResult = wxMpService.getKefuService().sendKefuMessage(kefuMessage);
            if (sendResult) {
                redisUtils.catchChat(fromUser, responseMessages.getRole(), responseMessages.getContent());
            }
        } catch (WxErrorException e) {
            e.printStackTrace();
            serverErrorKefuReplay(wechatTextMessage.getFromUser());
        } finally {
            redisUtils.releaseLock(wechatTextMessage.getFromUser());
        }
    }
    /**
     * 被关注回复欢迎语
     *
     * @param weChatSubscribeEvents
     */
    @Override
    public void subscribeEvent(WxMpXmlMessage weChatSubscribeEvents) {
        try {
//            ChatMessage actualChatMessage = new ChatMessage(GptRoleType.SYSTEM.getRole(), WELCOME_WORDS);
            String fromUser = weChatSubscribeEvents.getFromUser();
//            redisUtils.catchChat(fromUser, GptRoleType.SYSTEM.getRole(), WELCOME_WORDS);
//            ChatMessage responseMessages = getResponseMessages(actualChatMessage, fromUser);
            WxMpKefuMessage kefuMessage = getWxMpKefuMessage(WELCOME_WORDS, fromUser);
            StringBuilder original = new StringBuilder(kefuMessage.getContent());
            kefuMessage.setContent(original.append(DEFAULT_WELCOME_WORDS_END).toString());
            wxMpService.getKefuService().sendKefuMessage(kefuMessage);
        } catch (WxErrorException e) {
            e.printStackTrace();
            serverErrorKefuReplay(weChatSubscribeEvents.getFromUser());
        } finally {
            redisUtils.releaseLock(weChatSubscribeEvents.getFromUser());
        }
    }

    /**
     * 异步回复语音事件
     *
     * @param voiceEvents
     */
    @Override
    public void voiceEvent(WxMpXmlMessage voiceEvents) {
        try {
            // 获取微信的语音识别
            String recognition = voiceEvents.getRecognition();
            String fromUser = voiceEvents.getFromUser();
            // 缓存
            redisUtils.catchChat(fromUser, GptRoleType.USER.getRole(), recognition);
            // 整理推送
            ChatMessage actualChatMessage = new ChatMessage(GptRoleType.USER.getRole(), recognition);
            ChatMessage responseMessages = getResponseMessages(actualChatMessage, fromUser);
            WxMpKefuMessage kefuMessage = getWxMpKefuMessage(responseMessages.getContent(), fromUser);
            boolean sendResult = wxMpService.getKefuService().sendKefuMessage(kefuMessage);
            if (sendResult) {
                redisUtils.catchChat(fromUser, responseMessages.getRole(), responseMessages.getContent());
            }
        } catch (WxErrorException e) {
            e.printStackTrace();
            serverErrorKefuReplay(voiceEvents.getFromUser());
        } finally {
            redisUtils.releaseLock(voiceEvents.getFromUser());
        }
    }

    /**
     * 微信群分享
     * @param dataInfo
     */
    @Override
    public void chatGroupShare(WxMpXmlMessage dataInfo) {
        try {
            String fromUser = dataInfo.getFromUser();
            String mediaId = uploadImageAndGetMediaId("Group chat sharing/微信群邀请链接.jpg");
            WxMpKefuMessage kefuMessage = WxMpKefuMessage.IMAGE().toUser(fromUser).mediaId(mediaId).build();
            wxMpService.getKefuService().sendKefuMessage(kefuMessage);
        } catch (Exception e) {
            e.printStackTrace();
            serverErrorKefuReplay(dataInfo.getFromUser());
        } finally {
            redisUtils.releaseLock(dataInfo.getFromUser());
        }
    }

    // 上传图片并获取media_id
    public String uploadImageAndGetMediaId(String alyDataName) throws Exception {
        String accessKeyId = "LTAI5tA2tf4MXbRJHg9z5NQq";
        String accessKeySecret = "S6CyPTrtqK3Lb2ZG69jWREXFXE2tQO";
        String endpoint = "https://oss-us-west-1.aliyuncs.com";
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            // 调用ossClient.getObject返回一个OSSObject实例，该实例包含文件内容及文件元信息。
            OSSObject ossObject = ossClient.getObject("wechat-gpt", alyDataName);
            InputStream data = ossObject.getObjectContent();
            // 上传图片并获取media_id
            WxMediaUploadResult wxMediaUploadResult = wxMpService.getMaterialService().mediaUpload(WxConsts.XmlMsgType.IMAGE, "jpg", data);
            return wxMediaUploadResult.getMediaId();
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return null;
    }
    /**
     * 获取GPT回复
     *
     * @param actualChatMessage
     * @param fromUser
     * @return
     */
    private ChatMessage getResponseMessages(ChatMessage actualChatMessage, String fromUser){
        WxRedisCatchVo aCatch = redisUtils.getCatch(fromUser);
        ArrayList<ChatMessage> messages = new ArrayList<>();
        if (ObjectUtils.isEmpty(aCatch)) {
            messages.add(actualChatMessage);
            return gptUtils.askGpt(messages);
        }
        messages = aCatch.getChatCatch();
        return gptUtils.askGpt(messages);
    }

    /**
     * 获取WxMpKefuMessage
     *
     * @param responseMessages
     * @param fromUserName
     * @return
     */
    private WxMpKefuMessage getWxMpKefuMessage(String responseMessages, String fromUserName) {
        return WxMpKefuMessage.TEXT()
                .toUser(fromUserName)
                .content(responseMessages)
                .build();
    }


    /**
     * 服务出错时的友好回复
     *
     * @param fromUserName
     */
    private void serverErrorKefuReplay(String fromUserName) {
        try {
            wxMpService.getKefuService().sendKefuMessage(
                    WxMpKefuMessage.TEXT()
                            .toUser(fromUserName)
                            .content(SERVER_ERROR_REPLAY)
                            .build()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
