package com.gpt.chatproject.enums;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING("待支付"),
    PAID("已支付"),
    CANCELED("已支付");

    private final String type;

    OrderStatus(String type) {
        this.type = type;
    }
}
