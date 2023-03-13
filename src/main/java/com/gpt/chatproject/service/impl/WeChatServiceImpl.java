package com.gpt.chatproject.service.impl;

import com.gpt.chatproject.service.WeChatService;
import com.gpt.chatproject.utils.GptUtils;
import com.gpt.chatproject.vo.RedisLock;
import com.theokanning.openai.completion.chat.ChatMessage;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.kefu.WxMpKefuMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class WeChatServiceImpl implements WeChatService {
    @Autowired
    private GptUtils gptUtils;

    @Autowired
    private WxMpService wxMpService;

    @Autowired
    private RedisLock redisLock;

    @Value("${openai.welcome_words}")
    private String WELCOME_WORDS;

    @Value("${wxchat.default_welcome_words_end}")
    private String DEFAULT_WELCOME_WORDS_END;

    @Override
    public void weChatAsyncReply(WxMpXmlMessage wechatTextMessage){
        try {
            ChatMessage chatMessage = new ChatMessage("user", wechatTextMessage.getContent());
            WxMpKefuMessage kefuMessage = getReply(chatMessage, wechatTextMessage.getFromUser());
            wxMpService.getKefuService().sendKefuMessage(kefuMessage);
        } catch (WxErrorException e) {
            e.printStackTrace();
        } finally {
            redisLock.releaseLock(wechatTextMessage.getFromUser());
        }
    }

    @Override
    public void subscribeEvent(WxMpXmlMessage weChatSubscribeEvents){
        try {
            ChatMessage chatMessage = new ChatMessage("system", WELCOME_WORDS);
            WxMpKefuMessage kefuMessage = getReply(chatMessage, weChatSubscribeEvents.getFromUser());
            StringBuilder original = new StringBuilder(kefuMessage.getContent());
            kefuMessage.setContent(original.append(DEFAULT_WELCOME_WORDS_END).toString());
            wxMpService.getKefuService().sendKefuMessage(kefuMessage);
        } catch (WxErrorException e) {
            e.printStackTrace();
        } finally {
            redisLock.releaseLock(weChatSubscribeEvents.getFromUser());
        }
    }

    WxMpKefuMessage getReply(ChatMessage chatMessage, String fromUserName) throws WxErrorException {
        ArrayList<ChatMessage> messages = new ArrayList<>();
        messages.add(chatMessage);
        ChatMessage responseMessages = gptUtils.askGpt(messages);
        String responseContent = responseMessages.getContent();
        return WxMpKefuMessage.TEXT()
                .toUser(fromUserName)
                .content(responseContent)
                .build();
    }
}
