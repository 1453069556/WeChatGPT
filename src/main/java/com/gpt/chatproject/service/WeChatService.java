package com.gpt.chatproject.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import org.springframework.stereotype.Service;

@Service
public interface WeChatService {
    /**
     * 过滤条件
     * @param wxMpXmlMessage
     * @throws WxErrorException
     */
    String shouldFilterMessage(WxMpXmlMessage wxMpXmlMessage) throws WxErrorException, JsonProcessingException;

    /**
     * 异步回复文本消息
     * @param wechatTextMessage
     * @throws WxErrorException
     */
    void textEvent(WxMpXmlMessage wechatTextMessage) throws WxErrorException;

    /**
     * 被关注回复欢迎语
     * @param weChatSubscribeEvents
     * @throws WxErrorException
     */
    void subscribeEvent(WxMpXmlMessage weChatSubscribeEvents) throws WxErrorException;

    /**
     * 异步回复语音事件
     * @param voiceEvents
     * @throws WxErrorException
     */
    void voiceEvent(WxMpXmlMessage voiceEvents) throws WxErrorException;

    /**
     * 微信群分享
     * @param dataInfo
     */
    void chatGroupShare(WxMpXmlMessage dataInfo);
}
