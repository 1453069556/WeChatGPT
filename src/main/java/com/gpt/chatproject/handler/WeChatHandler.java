package com.gpt.chatproject.handler;

import com.gpt.chatproject.service.WeChatService;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
public class WeChatHandler {
    @Autowired
    private WeChatService weChatService;

    @Value("${wxchat.tips}")
    private String TIPS;

    @Value("${wxchat.welcome_words}")
    private String WELCOME_WORDS;

    public WxMpMessageHandler getSubscribeEventHandler() {
        return (wxMessage, context, wxMpService, sessionManager) ->
                WxMpXmlOutMessage.TEXT().fromUser(wxMessage.getToUser())
                        .toUser(wxMessage.getFromUser())
                        .content(WELCOME_WORDS).build();
    }

    public WxMpMessageHandler getWeChatAsyncReplyHandler() {
        return (wxMessage, context, wxMpService, sessionManager) -> {
            try {
                weChatService.textEvent(wxMessage);
            } catch (WxErrorException e) {
                e.printStackTrace();
            }
            return null;
        };
    }

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

    public WxMpMessageHandler getChatGroupShareHandler() {
        return (wxMessage, context, wxMpService, sessionManager) -> {
            try {
                weChatService.chatGroupShare(wxMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        };
    }


    public WxMpMessageHandler getTipsButtonHandler() {
        return (wxMessage, context, wxMpService, sessionManager) ->
                WxMpXmlOutMessage.TEXT().fromUser(wxMessage.getToUser())
                        .toUser(wxMessage.getFromUser())
                        .content(TIPS).build();
    }

}
