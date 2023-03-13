package com.gpt.chatproject.config;

import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class WechatConfig {
    @Autowired
    private WxMpService wxMpService;

    @Bean
    public WxMpMessageRouter messageRouter(WxMpService wxMpService) {
        return new WxMpMessageRouter(wxMpService);
    }
}
