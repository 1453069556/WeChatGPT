package com.gpt.chatproject.enums;

import lombok.Getter;

@Getter
public enum GptRoleType {
    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant");

    private final String role;

    GptRoleType(String role) {
        this.role = role;
    }

}
