package com.gpt.chatproject.controller;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.gpt.chatproject.handler.WeChatHandler;
import com.gpt.chatproject.service.WeChatService;
import com.gpt.chatproject.utils.RedisUtils;
import com.gpt.chatproject.vo.WechatResponseTextMessage;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


@RestController
@RequestMapping("/wechat")
public class WechatController {

    @Value("${wxchat.frequency_response}")
    private String FREQUENCY_RESPONSE;

    @Value("${wxchat.max_tokens}")
    private Integer MAX_TOKENS;

    @Value("${wxchat.chars_overflow_response}")
    private String CHARS_OVERFLOW_RESPONSE;

    @Autowired
    private WxMpService wxMpService;

    @Autowired
    private WeChatHandler weChatHandler;

    @Autowired
    private WeChatService weChatService;

    @Autowired
    private WxMpMessageRouter messageRouter;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private XmlMapper xmlMapper;

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
    public String weChatPost(HttpServletRequest request) throws IOException {
        ServletInputStream inputStream = request.getInputStream();
        WxMpXmlMessage wxMpXmlMessage = WxMpXmlMessage.fromXml(inputStream);
        // 字数限制
        if (wxMpXmlMessage.getContent().length() > MAX_TOKENS){
            WechatResponseTextMessage wechatResponseTextMessage = new WechatResponseTextMessage(wxMpXmlMessage.getFromUser(),
                    wxMpXmlMessage.getToUser(), wxMpXmlMessage.getMsgType(), CHARS_OVERFLOW_RESPONSE);
            return xmlMapper.writeValueAsString(wechatResponseTextMessage);
        }
        // 一问一答限制
        if (!redisUtils.tryLock(wxMpXmlMessage.getFromUser())) {
            WechatResponseTextMessage wechatResponseTextMessage = new WechatResponseTextMessage(wxMpXmlMessage.getFromUser(),
                    wxMpXmlMessage.getToUser(), wxMpXmlMessage.getMsgType(), FREQUENCY_RESPONSE);
            return xmlMapper.writeValueAsString(wechatResponseTextMessage);
        }
        // 消息路由
        messageRouter
                // 路由用户关注事件
                .rule().msgType(WxConsts.XmlMsgType.EVENT)
                .event("subscribe")
                .handler(weChatHandler.getSubscribeEventHandler()).end()
                // 路由用户文本消息
                .rule().msgType(WxConsts.XmlMsgType.TEXT)
                .handler(weChatHandler.getWeChatAsyncReplyHandler()).end();
        messageRouter.route(wxMpXmlMessage);
        return "";
    }
}
