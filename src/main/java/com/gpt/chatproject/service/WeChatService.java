package com.gpt.chatproject.service;

import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import org.springframework.stereotype.Service;

@Service
public interface WeChatService {
    // 异步处理消息
    void weChatAsyncReply(WxMpXmlMessage wechatTextMessage) throws WxErrorException;
    // 被关注事件
    void subscribeEvent(WxMpXmlMessage weChatSubscribeEvents) throws WxErrorException;
}
