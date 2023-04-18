package com.gpt.chatproject.enums;

import lombok.Getter;

@Getter
public
enum RedisKeyEnum {
    MQ_QUEUE_COUNT("MQ_QUEUE_COUNT");

    private final String type;

    RedisKeyEnum(String type) {
        this.type = type;
    }
}
