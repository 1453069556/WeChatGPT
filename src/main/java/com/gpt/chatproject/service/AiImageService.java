package com.gpt.chatproject.service;

import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public interface AiImageService {
    /**
     * 以图生图
     *
     * @param wxImageMessage wxImageMessage
     */
    void imageVariation(WxMpXmlMessage wxImageMessage) throws IOException;


    /**
     * 通过沟通的方式使用prompt生成图片
     *
     * @param wxImageMessage wxImageMessage
     */
    void imageCreate(WxMpXmlMessage wxImageMessage) throws Exception;

}
