package com.gpt.chatproject.enums;


import lombok.Getter;

@Getter
public enum GptModelType {
    GPT_TURBO("gpt-3.5-turbo"),
    TEXT_DAVINCI("text-davinci-003"),
    CODE_DAVINCI("code-davinci-002");

    private final String type;

    GptModelType(String type) {
        this.type = type;
    }
}
