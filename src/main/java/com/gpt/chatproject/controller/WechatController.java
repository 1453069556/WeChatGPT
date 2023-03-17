package com.gpt.chatproject.controller;

import com.gpt.chatproject.handler.WeChatHandler;
import com.gpt.chatproject.service.WeChatService;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


@RestController
@RequestMapping("/wechat")
public class WechatController {


    @Autowired
    private WxMpService wxMpService;

    @Autowired
    private WeChatHandler weChatHandler;

    @Autowired
    private WxMpMessageRouter messageRouter;

    @Autowired
    private WeChatService weChatService;

    // 接入认证
    @GetMapping()
    public String verifyWechatServer(@RequestParam(name = "signature") String signature, @RequestParam(name = "timestamp") String timestamp, @RequestParam(name = "nonce") String nonce, @RequestParam(name = "echostr") String echostr) {
        if (!wxMpService.checkSignature(timestamp, nonce, signature)) {
            return null;
        }
        return echostr;
    }

    // 被关注和取关事件
    @PostMapping()
    public String weChatPost(HttpServletRequest request) throws IOException, WxErrorException {
        ServletInputStream inputStream = request.getInputStream();
        WxMpXmlMessage wxMpXmlMessage = WxMpXmlMessage.fromXml(inputStream);
        // 聊天过滤条件，如频率、字数等
        String filterMessage = weChatService.shouldFilterMessage(wxMpXmlMessage);
        //如果返回值不为空字符串则说明被拦截
        if (StringUtils.isNotBlank(filterMessage)) {
            return filterMessage;
        }
        // 消息路由
        messageRouter
                // 路由用户关注事件
                .rule().msgType(WxConsts.XmlMsgType.EVENT)
                .event("subscribe")
                .handler(weChatHandler.getSubscribeEventHandler()).end()
                // 路由用户文本消息
                .rule().msgType(WxConsts.XmlMsgType.TEXT)
                .handler(weChatHandler.getWeChatAsyncReplyHandler()).end()
                // 路由语音消息
                .rule().msgType(WxConsts.XmlMsgType.VOICE)
                .handler(weChatHandler.getWeChatVoiceReplyHandler()).end();
        messageRouter.route(wxMpXmlMessage);
        return "";
    }
}
