package com.gpt.chatproject.service.impl;

import com.gpt.chatproject.service.WeChatService;
import com.gpt.chatproject.utils.GptUtils;
import com.gpt.chatproject.utils.RedisUtils;
import com.gpt.chatproject.vo.WxRedisCatchVo;
import com.theokanning.openai.completion.chat.ChatMessage;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.kefu.WxMpKefuMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;

@Service
public class WeChatServiceImpl implements WeChatService {
    @Autowired
    private GptUtils gptUtils;

    @Autowired
    private WxMpService wxMpService;

    @Autowired
    private RedisUtils redisUtils;

    @Value("${openai.welcome_words}")
    private String WELCOME_WORDS;

    @Value("${wxchat.default_welcome_words_end}")
    private String DEFAULT_WELCOME_WORDS_END;

    @Override
    public void weChatAsyncReply(WxMpXmlMessage wechatTextMessage) {
        try {
            String content = wechatTextMessage.getContent();
            ChatMessage actualChatMessage = new ChatMessage("user", content);
            String fromUser = wechatTextMessage.getFromUser();
            redisUtils.catchChat(fromUser, "user", content);
            ChatMessage responseMessages = getResponseMessages(actualChatMessage, fromUser);
            WxMpKefuMessage kefuMessage = getWxMpKefuMessage(responseMessages, fromUser);
            boolean sendResult = wxMpService.getKefuService().sendKefuMessage(kefuMessage);
            if (sendResult) {
                redisUtils.catchChat(fromUser, responseMessages.getRole(), responseMessages.getContent());
            }
        } catch (WxErrorException e) {
            e.printStackTrace();
        } finally {
            redisUtils.releaseLock(wechatTextMessage.getFromUser());
        }
    }

    @Override
    public void subscribeEvent(WxMpXmlMessage weChatSubscribeEvents) {
        try {
            ChatMessage actualChatMessage = new ChatMessage("system", WELCOME_WORDS);
            String fromUser = weChatSubscribeEvents.getFromUser();
            redisUtils.catchChat(fromUser, "system", WELCOME_WORDS);
            ChatMessage responseMessages = getResponseMessages(actualChatMessage, fromUser);
            WxMpKefuMessage kefuMessage = getWxMpKefuMessage(responseMessages, fromUser);
            StringBuilder original = new StringBuilder(kefuMessage.getContent());
            kefuMessage.setContent(original.append(DEFAULT_WELCOME_WORDS_END).toString());
            wxMpService.getKefuService().sendKefuMessage(kefuMessage);
        } catch (WxErrorException e) {
            e.printStackTrace();
        } finally {
            redisUtils.releaseLock(weChatSubscribeEvents.getFromUser());
        }
    }

    // 获取GPT回复
    ChatMessage getResponseMessages(ChatMessage actualChatMessage, String fromUser) {
        WxRedisCatchVo aCatch = redisUtils.getCatch(fromUser);
        ArrayList<ChatMessage> messages = new ArrayList<>();
        if (ObjectUtils.isEmpty(aCatch)) {
            messages.add(actualChatMessage);
            return gptUtils.askGpt(messages);
        }
        messages = aCatch.getChatCatch();
        return gptUtils.askGpt(messages);
    }

    // 获取WxMpKefuMessage
    WxMpKefuMessage getWxMpKefuMessage(ChatMessage responseMessages, String fromUserName) {
        String responseContent = responseMessages.getContent();
        return WxMpKefuMessage.TEXT()
                .toUser(fromUserName)
                .content(responseContent)
                .build();
    }
}
