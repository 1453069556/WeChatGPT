package com.gpt.chatproject.enums;

import lombok.Getter;

@Getter
public enum HttpEnum {
    HEADER_NAME("header_name"),
    HEADER_VALUE("header_value");

    private final String type;

    HttpEnum(String type) {
        this.type = type;
    }
}
