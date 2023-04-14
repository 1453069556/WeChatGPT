package com.gpt.chatproject.form.wechat;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.time.Instant;

@Data
@Component
@JacksonXmlRootElement(localName = "xml")
public class WechatResponseTextMessage {
    public WechatResponseTextMessage() {
        this.createTime = Instant.now().getEpochSecond();
    }

    public WechatResponseTextMessage(WechatTextMessage wechatTextMessage, String content) {
        this.toUserName = wechatTextMessage.getFromUserName();
        this.fromUserName = wechatTextMessage.getToUserName();
        this.createTime = Instant.now().getEpochSecond();
        this.msgType = "text";
        this.content = content;
    }

    public WechatResponseTextMessage(String toUserName, String fromUserName, String msgType, String content) {
        this.toUserName = toUserName;
        this.fromUserName = fromUserName;
        this.createTime = Instant.now().getEpochSecond();
        this.msgType = msgType;
        this.content = content;
    }

    @JacksonXmlProperty(localName = "ToUserName")
    private String toUserName;

    @JacksonXmlProperty(localName = "FromUserName")
    private String fromUserName;

    @JacksonXmlProperty(localName = "CreateTime")
    @Setter(AccessLevel.NONE)
    @NotNull
    private Long createTime;

    @JacksonXmlProperty(localName = "MsgType")
    private String msgType;

    @JacksonXmlProperty(localName = "Content")
    private String content;

}
