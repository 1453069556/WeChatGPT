package com.gpt.chatproject.service;

import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public interface AiImageService {
    /**
     * 以图生图-dall
     *
     * @param wxImageMessage wxImageMessage
     */
    void imageDallVariation(WxMpXmlMessage wxImageMessage) throws IOException, WxErrorException;


    /**
     * 通过沟通的方式使用prompt生成图片-dall
     *
     * @param wxImageMessage wxImageMessage
     */
    void imageDallCreate(WxMpXmlMessage wxImageMessage) throws Exception;


    /**
     * 通过沟通的方式使用prompt生成图片-MidjourneyMqVo
     *
     * @param wxImageMessage wxImageMessage
     */
    void imageMidjourneyCreate(WxMpXmlMessage wxImageMessage) throws Exception;

    /**
     * 以图生图-MidjourneyMqVo
     *
     * @param wxImageMessage wxImageMessage
     */
    void imageMidjourneyVariation(WxMpXmlMessage wxImageMessage) throws Exception;

    /**
     * 根据衍生指令生成图片
     * @param wxMessage
     * @throws WxErrorException
     */
    void imageMidjourneyCustom(WxMpXmlMessage wxMessage) throws WxErrorException;

    /**
     * mj的懒人模式，仅支持图生图无需指令
     * @param wxMessage
     */
    void imageMidjourneyLazy(WxMpXmlMessage wxMessage) throws IOException;
}
