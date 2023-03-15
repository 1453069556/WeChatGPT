package com.gpt.chatproject.vo;

import com.theokanning.openai.completion.chat.ChatMessage;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;

@Data
public class WxRedisCatchVo implements Serializable {
    public WxRedisCatchVo(Integer maxCatch) {
        this.chatCount = 0;
        this.maxCatch = maxCatch;
        this.chatCatch = new ArrayList<>();
    }

    public WxRedisCatchVo() {
        this.chatCount = 0;
        this.maxCatch = 4;
        this.chatCatch = new ArrayList<>();
    }

    public WxRedisCatchVo(ArrayList<ChatMessage> chatCatch, Integer maxCatch) {
        this.chatCount = 0;
        this.maxCatch = maxCatch;
        this.chatCatch = chatCatch;
    }

    // 聊天次数统计
    private Integer chatCount;
    // 最大缓存量
    private Integer maxCatch;
    // 聊天记录
    private ArrayList<ChatMessage> chatCatch;

    public void setChatCatch(ArrayList<ChatMessage> chatCatch) {
        if (chatCatch.size() > this.maxCatch) {
            chatCatch.subList(0, chatCatch.size() - this.maxCatch).clear();
        }
        this.chatCatch = chatCatch;
    }
}
