package com.gpt.chatproject.service.impl;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.gpt.chatproject.dao.FansDao;
import com.gpt.chatproject.entity.Fans;
import com.gpt.chatproject.enums.GptRoleType;
import com.gpt.chatproject.form.wechat.WechatResponseTextMessage;
import com.gpt.chatproject.service.WeChatService;
import com.gpt.chatproject.utils.RedisUtils;
import com.gpt.chatproject.utils.WeChatUtils;
import com.gpt.chatproject.vo.WxRedisCatchVo;
import com.theokanning.openai.completion.chat.ChatMessage;
import lombok.extern.log4j.Log4j2;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.kefu.WxMpKefuMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@Log4j2
public class WeChatServiceImpl implements WeChatService {
    @Autowired
    private WxMpService wxMpService;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private XmlMapper xmlMapper;
    @Autowired
    private WeChatUtils weChatUtils;
    @Autowired
    private FansDao fansDao;
    @Value("${wxchat.release_lock_replay}")
    private String RELEASE_LOCK_REPLAY;
    @Value("${wxchat.max_send_tokens}")
    private Integer MAX_SEND_TOKENS;
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
            WxRedisCatchVo aCatch = redisUtils.getCatch(fromUser);
            String content = wxMpXmlMessage.getContent();
            switch (aCatch.getChatType()) {
                case IMAGE_MIDJOURNEY:
                    if (content.startsWith("/modifier") || content.startsWith("/imagine") || content.startsWith("MJ::JOB::")) {
                        return weChatUtils.getLock(fromUser, wxMpXmlMessage, 3);
                    }
                case IMAGE_DALL:
                    if (content.startsWith("/imagine")) {
                        return weChatUtils.getLock(fromUser, wxMpXmlMessage, 3);
                    }
                default:
                    return weChatUtils.getLock(fromUser, wxMpXmlMessage, 1);
            }
        }
        // 是语音消息才做以下处理
        if (WxConsts.XmlMsgType.VOICE.equals(msgType)) {
            // 字数限制
            if (wxMpXmlMessage.getRecognition().length() > MAX_SEND_TOKENS) {
                result = xmlMapper.writeValueAsString(new WechatResponseTextMessage(fromUser,
                        wxMpXmlMessage.getToUser(), WxConsts.XmlMsgType.TEXT, CHARS_OVERFLOW_RESPONSE));
                return result;
            }
            return weChatUtils.getLock(fromUser, wxMpXmlMessage, 1);
        }
        // 是图片消息才做以下处理
        if (WxConsts.XmlMsgType.IMAGE.equals(msgType)) {
            WxRedisCatchVo aCatch = redisUtils.getCatch(fromUser);
            switch (aCatch.getChatType()){
                case IMAGE_MIDJOURNEY:
                case IMAGE_DALL:
                    return weChatUtils.getLock(fromUser, wxMpXmlMessage, 3);
                default:
                    return weChatUtils.getLock(fromUser, wxMpXmlMessage, 1);
            }
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
                weChatUtils.sendKefuTextMessage(inviteder, RELEASE_LOCK_REPLAY);
            }
        }
    }

    /**
     * 异步回复文本消息
     *
     * @param wechatTextMessage wechatTextMessage
     */
    @Override
    public void textEvent(WxMpXmlMessage wechatTextMessage) {
        try {
            String content = wechatTextMessage.getContent();
            ChatMessage actualChatMessage = new ChatMessage(GptRoleType.USER.getRole(), content);
            String fromUser = wechatTextMessage.getFromUser();
            redisUtils.catchChat(fromUser, GptRoleType.USER.getRole(), content);
            weChatUtils.sendKefuMessages(fromUser, actualChatMessage);
        } catch (Exception e) {
            weChatUtils.serverErrorKefuReplay(wechatTextMessage.getFromUser());
            throw new RuntimeException(e);
        } finally {
            redisUtils.releaseChatLock(wechatTextMessage.getFromUser());
        }
    }

    /**
     * 异步回复语音事件
     *
     * @param voiceEvents voiceEvents
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
            weChatUtils.sendKefuMessages(fromUser, actualChatMessage);
        } catch (Exception e) {
            log.debug(e.getMessage());
            e.printStackTrace();
            weChatUtils.serverErrorKefuReplay(voiceEvents.getFromUser());
        } finally {
            redisUtils.releaseChatLock(voiceEvents.getFromUser());
        }
    }

    /**
     * 微信群分享
     *
     * @param dataInfo dataInfo
     */
    @Override
    public void chatGroupShare(WxMpXmlMessage dataInfo) {
        try {
            String fromUser = dataInfo.getFromUser();
            String mediaId = weChatUtils.uploadImageAndGetMediaId("Group chat sharing/微信群邀请链接.jpg");
            weChatUtils.sendKefuImageMessage(fromUser, mediaId);
        } catch (Exception e) {
            e.printStackTrace();
            log.debug(e.getMessage());
//            serverErrorKefuReplay(dataInfo.getFromUser());
        } finally {
            redisUtils.releaseChatLock(dataInfo.getFromUser());
        }
    }

    @Override
    public void imageEvent(WxMpXmlMessage wxImageMessage) throws WxErrorException {
        String fromUser = wxImageMessage.getFromUser();
        if (!redisUtils.tryAiPicLock(fromUser)) {
            weChatUtils.sendKefuTextMessage(fromUser, "您有未处理完的图片正在处理，请耐心等待！");
            redisUtils.releaseChatLock(fromUser);
            return;
        }
        try {
            WxMpKefuMessage imageMessage = WxMpKefuMessage.TEXT().toUser(fromUser)
                    .content("小C图片聊天互动正在学习中噢，如需绘图请进入绘图模式。").build();
            wxMpService.getKefuService().sendKefuMessage(imageMessage);
        } finally {
            redisUtils.releasePicLock(fromUser);
            redisUtils.releaseChatLock(fromUser);
        }
    }

}
