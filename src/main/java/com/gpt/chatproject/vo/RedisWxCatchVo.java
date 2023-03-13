package com.gpt.chatproject.vo;

import com.theokanning.openai.completion.chat.ChatMessage;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class RedisWxCatchVo implements Serializable {
    // 聊天次数统计
    private Integer chatCount;
    // 聊天频率锁
    private Integer frequencyLock;
    // 聊天记录
    private List<ChatMessage> chatCatch;
}
