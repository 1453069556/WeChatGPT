package com.gpt.chatproject.enums;


import lombok.Getter;

@Getter
public enum RedisLockType {
    IMAGE_MIDJOURNEY(3),
    NORMAL(1);

    private final int delta;

    RedisLockType(int delta) {
        this.delta = delta;
    }
}
