package com.gpt.chatproject.handler;

import com.gpt.chatproject.enums.ChatType;
import com.gpt.chatproject.service.AiImageService;
import com.gpt.chatproject.service.MemberService;
import com.gpt.chatproject.service.WeChatService;
import com.gpt.chatproject.utils.RedisUtils;
import com.gpt.chatproject.utils.WeChatUtils;
import com.gpt.chatproject.vo.WxRedisCatchVo;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class WeChatHandler {
    @Autowired
    private WeChatService weChatService;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private WeChatUtils weChatUtils;
    @Autowired
    private AiImageService aiImageService;
    @Autowired
    private MemberService memberService;
    @Value("${wxchat.welcome_words}")
    private String WELCOME_WORDS;
    @Value("${wxchat.default_welcome_words_end}")
    private String DEFAULT_WELCOME_WORDS_END;
    @Value("${wxchat.update_success}")
    private String UPDATE_SUCCESS;
    @Value("${wxchat.update_fails}")
    private String UPDATE_FAILS;
    @Value("${wxchat.reset_success}")
    private String RESET_CHAT_SUCCESS;
    @Value("${openai.image_chat_answer}")
    private String IMAGE_CHAT_ANSWER;

    /**
     * 回复关注语
     *
     * @return
     */
    public WxMpMessageHandler getSubscribeEventHandler() {
        return (wxMessage, context, wxMpService, sessionManager) -> WxMpXmlOutMessage.TEXT().fromUser(wxMessage.getToUser()).toUser(wxMessage.getFromUser()).content(WELCOME_WORDS + DEFAULT_WELCOME_WORDS_END).build();
    }

    /**
     * 异步处理数据库相关操作
     *
     * @return
     */
    public WxMpMessageHandler getInvitedEventDBHandler() {
        return (wxMessage, context, wxMpService, sessionManager) -> {
            weChatService.invitedDBEvent(wxMessage);
            return null;
        };
    }

    /**
     * 异步回复文本消息
     *
     * @return
     */
    public WxMpMessageHandler getWeChatAsyncReplyHandler() {
        return (wxMessage, context, wxMpService, sessionManager) -> {
            String fromUser = wxMessage.getFromUser();
            try {
                WxRedisCatchVo aCatch = redisUtils.getCatch(fromUser);
                wxMessage.setContent(wxMessage.getContent().replaceFirst("^\\s+", ""));
                switch (aCatch.getChatType()) {
                    case NORMAL:
                        weChatService.textEvent(wxMessage);
                        break;
                    case IMAGE_MIDJOURNEY:
                        // 触发了图片prompt指令,生成图片
                        if (wxMessage.getContent().startsWith("/modifier")) {
                            aiImageService.imageMidjourneyVariation(wxMessage);
                            break;
                        } else if (wxMessage.getContent().startsWith("/imagine")) {
                            aiImageService.imageMidjourneyCreate(wxMessage);
                            break;
                        } else if (wxMessage.getContent().startsWith("MJ::JOB::")) {
                            aiImageService.imageMidjourneyCustom(wxMessage);
                            break;
                        }
                        String sendContent = weChatService.textEvent(wxMessage);
                        if (sendContent.replaceFirst("^\\s+", "").startsWith("/imagine")) {
                            weChatUtils.sendKefuTextMessage(fromUser, IMAGE_CHAT_ANSWER);
                        }
                        break;
                    case IMAGE_DALL:
                        // 触发了图片prompt指令,生成图片
                        if (wxMessage.getContent().startsWith("/imagine")) {
                            aiImageService.imageDallCreate(wxMessage);
                            break;
                        }
                        weChatService.textEvent(wxMessage);
                        break;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        };
    }

    /**
     * 语音处理
     *
     * @return
     */
    public WxMpMessageHandler getWeChatVoiceReplyHandler() {
        return (wxMessage, context, wxMpService, sessionManager) -> {
            try {
                weChatService.voiceEvent(wxMessage);
            } catch (WxErrorException e) {
                e.printStackTrace();
            }
            return null;
        };
    }

    /**
     * 图片处理
     *
     * @return
     */
    public WxMpMessageHandler getWeChatImageReplyHandler() {
        return (wxMessage, context, wxMpService, sessionManager) -> {
            String fromUser = wxMessage.getFromUser();
            try {
                WxRedisCatchVo aCatch = redisUtils.getCatch(fromUser);
                switch (aCatch.getChatType()) {
                    case IMAGE_MIDJOURNEY:
                        aiImageService.imageMidjourneyVariation(wxMessage);
                        break;
                    case IMAGE_DALL:
                        aiImageService.imageDallVariation(wxMessage);
                        break;
                    case NORMAL:
                        weChatService.imageEvent(wxMessage);
                        break;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        };
    }

    /**
     * 异步执行的按钮事件
     *
     * @return
     */
    public WxMpMessageHandler asyncButtonEvent() {
        return (wxMessage, context, wxMpService, sessionManager) -> {
            try {

                switch (wxMessage.getEventKey()) {
                    case "JOIN_GROUP_POST":
                        weChatService.chatGroupShare(wxMessage);
                        break;
                    case "AI_IMAGE_CHAT_DALL":
                        // TODO 暂时关闭DALL绘图功能
                        if (redisUtils.updateChatCatchType(wxMessage.getFromUser(), ChatType.IMAGE_DALL)) {
                            weChatUtils.sendKefuTextMessage(wxMessage.getFromUser(), UPDATE_SUCCESS);
                        } else {
                            weChatUtils.sendKefuTextMessage(wxMessage.getFromUser(), UPDATE_FAILS);
                        }
                        break;
                    case "AI_IMAGE_CHAT_MIDJOURNEY":
                        if (redisUtils.updateChatCatchType(wxMessage.getFromUser(), ChatType.IMAGE_MIDJOURNEY)) {
                            weChatUtils.sendKefuTextMessage(wxMessage.getFromUser(), UPDATE_SUCCESS);
                        } else {
                            weChatUtils.sendKefuTextMessage(wxMessage.getFromUser(), UPDATE_FAILS);
                        }
                        break;
                    case "RESET_CHAT_TYPE":
                        if (redisUtils.updateChatCatchType(wxMessage.getFromUser(), ChatType.NORMAL)) {
                            weChatUtils.sendKefuTextMessage(wxMessage.getFromUser(), RESET_CHAT_SUCCESS);
                        } else {
                            weChatUtils.sendKefuTextMessage(wxMessage.getFromUser(), UPDATE_FAILS);
                        }
                        break;
                    case "GET_MEMBER_INFO":
                        String memberInfo = memberService.getMemberInfo(wxMessage);
                        weChatUtils.sendKefuTextMessage(wxMessage.getFromUser(), memberInfo);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        };
    }

}
