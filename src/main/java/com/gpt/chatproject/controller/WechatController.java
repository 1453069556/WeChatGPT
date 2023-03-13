package com.gpt.chatproject.controller;

import com.gpt.chatproject.service.WeChatService;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


@RestController
@RequestMapping("/wechat")
public class WechatController {

    @Autowired
    private WxMpService wxMpService;

    @Autowired
    private WeChatService weChatService;

    @Autowired
    private WxMpMessageRouter messageRouter;

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
        messageRouter
                // 路由用户关注事件
                .rule().msgType(WxConsts.XmlMsgType.EVENT)
                .event("subscribe")
                .handler((wxMessage, context, wxMpService, sessionManager) -> {
                    try {
                        weChatService.subscribeEvent(wxMessage);
                    } catch (WxErrorException e) {
                        e.printStackTrace();
                    }
                    return null;
                }).end()
                // 路由用户文本消息
                .rule().msgType(WxConsts.XmlMsgType.TEXT)
                .handler((wxMessage, context, wxMpService, sessionManager) -> {
                    try {
                        weChatService.weChatAsyncReply(wxMessage);
                    } catch (WxErrorException e) {
                        e.printStackTrace();
                    }
                    return null;
                }).end();
        WxMpXmlMessage wxMpXmlMessage = WxMpXmlMessage.fromXml(request.getInputStream());
        WxMpXmlOutMessage route = messageRouter.route(wxMpXmlMessage);
        return "";
    }
}
