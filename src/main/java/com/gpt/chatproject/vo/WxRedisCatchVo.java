package com.gpt.chatproject.vo;

import com.gpt.chatproject.enums.ChatType;
import com.theokanning.openai.completion.chat.ChatMessage;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;

@Data
@NoArgsConstructor
public class WxRedisCatchVo implements Serializable {
    public WxRedisCatchVo(Integer maxCatch) {
        this.chatCount = 0;
        this.chatType = ChatType.NORMAL;
        this.maxCatch = maxCatch;
        this.imageNum = 0;
        this.chatCatch = new ArrayList<>();
    }

    public WxRedisCatchVo(Integer maxCatch, ChatType chatType) {
        this.chatCount = 0;
        this.chatType = chatType;
        this.maxCatch = maxCatch;
        this.imageNum = 0;
        this.chatCatch = new ArrayList<>();
    }

    public WxRedisCatchVo(ArrayList<ChatMessage> chatCatch, Integer maxCatch) {
        this.chatCount = 0;
        this.chatType = ChatType.NORMAL;
        this.maxCatch = maxCatch;
        this.imageNum = 0;
        this.chatCatch = chatCatch;
    }

    // 聊天次数统计
    private Integer chatCount;
    // 聊天模式
    private ChatType chatType;
    // 最大缓存量
    private Integer maxCatch;
    // 聊天记录
    private ArrayList<ChatMessage> chatCatch;
    // 身份
    private String memberLevel;
    // 绘图次数
    private int imageNum;

    public void setChatCatch(ArrayList<ChatMessage> chatCatch) {
        if (chatCatch.size() > this.maxCatch) {
            chatCatch.subList(0, chatCatch.size() - this.maxCatch).clear();
        }
        this.chatCatch = chatCatch;
    }
}
