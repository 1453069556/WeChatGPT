package com.gpt.chatproject.enums;

import lombok.Getter;

@Getter
public enum ChatType {
    IMAGE("image"),
    SPOKEN("spoken"),
    NORMAL("normal");

    private final String type;

    ChatType(String type) {
        this.type = type;
    }
}
