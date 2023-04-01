package com.gpt.chatproject.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.gpt.chatproject.dao.FansDao;
import com.gpt.chatproject.entity.Fans;
import com.gpt.chatproject.enums.GptRoleType;
import com.gpt.chatproject.service.WeChatService;
import com.gpt.chatproject.utils.GptUtils;
import com.gpt.chatproject.utils.MyStringUtils;
import com.gpt.chatproject.utils.RedisUtils;
import com.gpt.chatproject.utils.WeChatUtils;
import com.gpt.chatproject.vo.WechatResponseTextMessage;
import com.gpt.chatproject.vo.WxRedisCatchVo;
import com.theokanning.openai.completion.chat.ChatMessage;
import lombok.extern.log4j.Log4j2;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.result.WxMediaUploadResult;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.kefu.WxMpKefuMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
public class WeChatServiceImpl implements WeChatService {
    @Autowired
    private GptUtils gptUtils;

    @Autowired
    private WxMpService wxMpService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private XmlMapper xmlMapper;

    @Autowired
    private MyStringUtils myStringUtils;

    @Autowired
    private WeChatUtils weChatUtils;

    @Autowired
    private FansDao fansDao;

    @Value("${wxchat.server_error_replay}")
    private String SERVER_ERROR_REPLAY;
    @Value("${wxchat.release_lock_replay}")
    private String RELEASE_LOCK_REPLAY;

    @Value("${wxchat.max_send_tokens}")
    private Integer MAX_SEND_TOKENS;
    @Value("${wxchat.max_replay_tokens}")
    private Integer MAX_REPLAY_TOKENS;

    @Value("${wxchat.chars_overflow_response}")
    private String CHARS_OVERFLOW_RESPONSE;

    @Override
    public String shouldFilterMessage(WxMpXmlMessage wxMpXmlMessage) throws Exception {
        String fromUser = wxMpXmlMessage.getFromUser();
        String msgType = wxMpXmlMessage.getMsgType();
        String result;
        // 是文本消息才做以下处理
        if (WxConsts.XmlMsgType.TEXT.equals(msgType)) {
            // 字数限制
            if (wxMpXmlMessage.getContent().length() > MAX_SEND_TOKENS) {
                result = xmlMapper.writeValueAsString(new WechatResponseTextMessage(fromUser,
                        wxMpXmlMessage.getToUser(), WxConsts.XmlMsgType.TEXT, CHARS_OVERFLOW_RESPONSE));
                return result;
            }
            return weChatUtils.getLock(fromUser, wxMpXmlMessage);
        }
        // 是语音消息才做以下处理
        if (WxConsts.XmlMsgType.VOICE.equals(msgType)) {
            // 字数限制
            if (wxMpXmlMessage.getRecognition().length() > MAX_SEND_TOKENS) {
                result = xmlMapper.writeValueAsString(new WechatResponseTextMessage(fromUser,
                        wxMpXmlMessage.getToUser(), WxConsts.XmlMsgType.TEXT, CHARS_OVERFLOW_RESPONSE));
                return result;
            }
            return weChatUtils.getLock(fromUser, wxMpXmlMessage);
        }
        return "";
    }

    @Override
    public void invitedDBEvent(WxMpXmlMessage wxMpXmlMessage) throws WxErrorException {
        // 关注公众号的用户
        String fromUser = wxMpXmlMessage.getFromUser();
        // 邀请者
        String inviteder = null;
        String eventKey = wxMpXmlMessage.getEventKey();
        if (eventKey != null && eventKey.length() > 0) {
            inviteder = eventKey.substring(Math.max(0, eventKey.length() - 28));
        }
        if (StringUtils.isNotBlank(inviteder)) {
            // 录入未关注过的用户
            Fans fansByUserId = fansDao.getFansByUserId(fromUser);
            if (ObjectUtils.isEmpty(fansByUserId)) {
                Fans fans = new Fans();
                fans.setUserId(fromUser);
                fans.setInviterId(inviteder);
                fansDao.saveFans(fans);
                // 将邀请人的频率锁去除
                redisUtils.releaseTimeLock(inviteder);
                WxMpKefuMessage kefuMessage = WxMpKefuMessage.TEXT().toUser(inviteder).content(RELEASE_LOCK_REPLAY).build();
                wxMpService.getKefuService().sendKefuMessage(kefuMessage);
            }
        }
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
            sendKefuMessages(fromUser, actualChatMessage);
        } catch (Exception e) {
            log.debug(e.getMessage());
            e.printStackTrace();
            serverErrorKefuReplay(wechatTextMessage.getFromUser());
        } finally {
            redisUtils.releaseChatLock(wechatTextMessage.getFromUser());
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
            sendKefuMessages(fromUser, actualChatMessage);
        } catch (Exception e) {
            log.debug(e.getMessage());
            e.printStackTrace();
            serverErrorKefuReplay(voiceEvents.getFromUser());
        } finally {
            redisUtils.releaseChatLock(voiceEvents.getFromUser());
        }
    }

    /**
     * 发送客服消息公用方法
     *
     * @param fromUser
     * @param chatMessage
     * @throws WxErrorException
     */
    private void sendKefuMessages(String fromUser, ChatMessage chatMessage) throws Exception {
        ChatMessage responseMessages = getResponseMessages(chatMessage, fromUser);
        ArrayList<WxMpKefuMessage> kefuMessages = getWxMpKefuMessage(responseMessages.getContent(), fromUser);
        for (WxMpKefuMessage message : kefuMessages) {
            boolean sendResult = wxMpService.getKefuService().sendKefuMessage(message);
            if (sendResult) {
                redisUtils.catchChat(fromUser, responseMessages.getRole(), responseMessages.getContent());
            }
        }
    }

    /**
     * 微信群分享
     *
     * @param dataInfo
     */
    @Override
    public void chatGroupShare(WxMpXmlMessage dataInfo) {
        try {
            String fromUser = dataInfo.getFromUser();
//            String mediaId = uploadImageAndGetMediaId("Group chat sharing/微信群邀请链接.jpg");
            String mediaId = uploadImageAndGetMediaId(new File("src/main/resources/wxResources/qrCode.jpg"));
            WxMpKefuMessage kefuMessage = WxMpKefuMessage.IMAGE().toUser(fromUser).mediaId(mediaId).build();
            wxMpService.getKefuService().sendKefuMessage(kefuMessage);
        } catch (Exception e) {
            e.printStackTrace();
            log.debug(e.getMessage());
            serverErrorKefuReplay(dataInfo.getFromUser());
        } finally {
            redisUtils.releaseChatLock(dataInfo.getFromUser());
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

    // 上传图片并获取media_id
    public String uploadImageAndGetMediaId(File image) throws Exception {
        // 上传图片并获取media_id
        WxMediaUploadResult wxMediaUploadResult = wxMpService.getMaterialService().mediaUpload(WxConsts.XmlMsgType.IMAGE, image);
        return wxMediaUploadResult.getMediaId();
    }

    /**
     * 获取GPT回复
     *
     * @param actualChatMessage
     * @param fromUser
     * @return
     */
    private ChatMessage getResponseMessages(ChatMessage actualChatMessage, String fromUser) throws Exception {
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
     * 获取WxMpKefuMessage列表
     *
     * @param responseMessages
     * @param fromUserName
     * @return
     */
    private ArrayList<WxMpKefuMessage> getWxMpKefuMessage(String responseMessages, String fromUserName) throws UnsupportedEncodingException {
        List<String> contents = myStringUtils.splitString(responseMessages, MAX_REPLAY_TOKENS);
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
    private void serverErrorKefuReplay(String fromUserName) {
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
}
