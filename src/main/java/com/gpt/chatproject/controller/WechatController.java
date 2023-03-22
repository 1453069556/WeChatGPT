package com.gpt.chatproject.controller;

import com.gpt.chatproject.handler.WeChatHandler;
import com.gpt.chatproject.service.WeChatService;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.apache.catalina.connector.Response;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
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
                // 路由菜单按钮消息
                .rule().async(true).msgType(WxConsts.XmlMsgType.EVENT)
                .event(WxConsts.EventType.CLICK)
                .eventKey("JOIN_GROUP_POST")
                .handler(weChatHandler.getChatGroupShareHandler()).end()
                // 路由TIPS按钮消息
                .rule().async(false).msgType(WxConsts.XmlMsgType.EVENT)
                .event(WxConsts.EventType.CLICK)
                .eventKey("TIPS")
                .handler(weChatHandler.getTipsButtonHandler()).end()
                // 路由用户关注事件
                .rule().async(false).msgType(WxConsts.XmlMsgType.EVENT)
                .event(WxConsts.EventType.SUBSCRIBE)
                .handler(weChatHandler.getSubscribeEventHandler()).end()
                // 路由用户文本消息
                .rule().async(true).msgType(WxConsts.XmlMsgType.TEXT)
                .handler(weChatHandler.getWeChatAsyncReplyHandler()).end()
                // 路由语音消息
                .rule().async(true).msgType(WxConsts.XmlMsgType.VOICE)
                .handler(weChatHandler.getWeChatVoiceReplyHandler()).end();
        WxMpXmlOutMessage outMessage = messageRouter.route(wxMpXmlMessage);
        if (outMessage == null) {
            //为null，返回空
            return "";
        }
        return outMessage.toXml();
    }
}
