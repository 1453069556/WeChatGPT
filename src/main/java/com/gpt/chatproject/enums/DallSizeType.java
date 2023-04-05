package com.gpt.chatproject.enums;

import lombok.Getter;

@Getter
public enum DallSizeType {
    SMALL("256x256"),
    MEDIUM("512x512"),
    LARGE("1024x1024");
    private final String type;

    DallSizeType(String type) {
        this.type = type;
    }
}
