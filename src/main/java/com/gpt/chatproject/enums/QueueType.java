package com.gpt.chatproject.enums;

import lombok.Getter;

@Getter
public enum QueueType {
    MIDJOURNEY("midjourney_catch"),
    CHAT_GPT("chat_gpt");

    private final String type;

    QueueType(String type) {
        this.type = type;
    }
}
