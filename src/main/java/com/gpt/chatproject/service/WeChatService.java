package com.gpt.chatproject.service;

import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import org.springframework.stereotype.Service;

@Service
public interface WeChatService {
    /**
     * 过滤条件
     *
     * @param wxMpXmlMessage wxMpXmlMessage
     * @throws WxErrorException
     */
    String shouldFilterMessage(WxMpXmlMessage wxMpXmlMessage) throws Exception;

    /**
     * 关注事件，处理数据库
     * @param wxMpXmlMessage
     */
    void invitedDBEvent(WxMpXmlMessage wxMpXmlMessage) throws WxErrorException;

    /**
     * 异步回复文本消息
     *
     * @param wechatTextMessage
     * @throws WxErrorException
     */
    String textEvent(WxMpXmlMessage wechatTextMessage) throws WxErrorException;

    /**
     * 异步回复语音事件
     *
     * @param voiceEvents
     * @throws WxErrorException
     */
    void voiceEvent(WxMpXmlMessage voiceEvents) throws WxErrorException;

    /**
     * 微信群分享
     *
     * @param dataInfo dataInfo
     */
    void chatGroupShare(WxMpXmlMessage dataInfo);

    /**
     * 图片事件
     * @param wxImageMessage
     */
    void imageEvent(WxMpXmlMessage wxImageMessage) throws WxErrorException;

}
