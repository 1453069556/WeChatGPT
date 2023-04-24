package com.gpt.chatproject.service;

import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import org.springframework.stereotype.Service;

@Service
public interface MemberService {

    String getMemberInfo(WxMpXmlMessage wxMpXmlMessage);
}
