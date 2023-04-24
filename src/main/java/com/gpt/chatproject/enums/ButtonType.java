package com.gpt.chatproject.enums;

import lombok.Getter;

@Getter
public enum ButtonType {
    JOIN_GROUP_POST("JOIN_GROUP_POST"),
    AI_IMAGE_CHAT_DALL("AI_IMAGE_CHAT_DALL"),
    AI_IMAGE_CHAT_MIDJOURNEY("AI_IMAGE_CHAT_MIDJOURNEY"),
    RESET_CHAT_TYPE("RESET_CHAT_TYPE");

    private final String type;

    ButtonType(String type) {
        this.type = type;
    }
}
