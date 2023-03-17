package com.gpt.chatproject.handler;

import com.gpt.chatproject.service.WeChatService;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class WeChatHandler {
    @Autowired
    private WeChatService weChatService;

    public WxMpMessageHandler getSubscribeEventHandler() {
        return (wxMessage, context, wxMpService, sessionManager) -> {
            try {
                weChatService.subscribeEvent(wxMessage);
            } catch (WxErrorException e) {
                e.printStackTrace();
            }
            return null;
        };
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
}
