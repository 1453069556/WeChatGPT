package com.gpt.chatproject.enums;

import lombok.Getter;

@Getter
public enum ChatType {
    IMAGE_MJ_LAZY("image_mj_lazy"),
    IMAGE_DALL("image_dall"),
    IMAGE_MIDJOURNEY("image_midjourney"),
    SPOKEN("spoken"),
    NORMAL("normal");

    private final String type;

    ChatType(String type) {
        this.type = type;
    }
}
