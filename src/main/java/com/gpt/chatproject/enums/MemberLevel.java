package com.gpt.chatproject.enums;

import lombok.Getter;

@Getter
public enum MemberLevel {
    REG("普通会员"),
    SIL("银卡会员"),
    GLD("金卡会员"),
    DIA("钻石会员");

    private final String type;

    MemberLevel(String type) {
        this.type = type;
    }
}
