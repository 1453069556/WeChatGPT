package com.gpt.chatproject.enums;

import lombok.Getter;

@Getter
public enum DallResponseType {
    URL("url"),
    B64_JSON("b64_json");

    private final String type;

    DallResponseType(String type) {
        this.type = type;
    }
}
