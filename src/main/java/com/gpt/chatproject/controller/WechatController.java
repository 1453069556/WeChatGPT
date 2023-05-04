package com.gpt.chatproject.controller;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.gpt.chatproject.form.Wechat.WechatResponseTextMessage;
import com.gpt.chatproject.handler.WeChatHandler;
import com.gpt.chatproject.service.WeChatService;
import com.gpt.chatproject.utils.RedisUtils;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/wechat")
public class WechatController {

    @Autowired
    private XmlMapper xmlMapper;
    @Autowired
    private RedisUtils redisUtils;
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

    @PostMapping()
    public String weChatPost(HttpServletRequest request) throws Exception {
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
                // 路由异步按钮消息
                .rule().async(true).msgType(WxConsts.XmlMsgType.EVENT)
                .event(WxConsts.EventType.CLICK)
                .handler(weChatHandler.asyncButtonEvent()).end()
                // 路由用户关注事件，异步处理数据库
                .rule().async(true).msgType(WxConsts.XmlMsgType.EVENT)
                .event(WxConsts.EventType.SUBSCRIBE)
                .handler(weChatHandler.getInvitedEventDBHandler()).next()
                // 路由用户关注事件，回复消息
                .rule().async(false).msgType(WxConsts.XmlMsgType.EVENT)
                .event(WxConsts.EventType.SUBSCRIBE)
                .handler(weChatHandler.getSubscribeEventHandler()).end()
                // 路由用户文本消息
                .rule().async(true).msgType(WxConsts.XmlMsgType.TEXT)
                .handler(weChatHandler.getWeChatAsyncReplyHandler()).end()
                // 路由语音消息
                .rule().async(true).msgType(WxConsts.XmlMsgType.VOICE)
                .handler(weChatHandler.getWeChatVoiceReplyHandler()).end()
                // 路由图片消息
                .rule().async(true).msgType(WxConsts.XmlMsgType.IMAGE)
                .handler(weChatHandler.getWeChatImageReplyHandler()).end();
        WxMpXmlOutMessage outMessage = messageRouter.route(wxMpXmlMessage);
        redisUtils.resetCatchExpire(wxMpXmlMessage.getFromUser());
        if (outMessage == null) {
            //为null，返回思考中
            return xmlMapper.writeValueAsString(
                    new WechatResponseTextMessage(wxMpXmlMessage.getFromUser(),
                            wxMpXmlMessage.getToUser(),
                            WxConsts.XmlMsgType.TEXT,
                            "思考中，请稍后~"
                    ));
        }
        return outMessage.toXml();
    }
}
