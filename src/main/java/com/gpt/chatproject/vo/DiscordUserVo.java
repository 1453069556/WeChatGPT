package com.gpt.chatproject.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
class DiscordUserVo implements Serializable {
    // 用户 ID
    @JsonProperty("id")
    private String id;

    // 用户名
    @JsonProperty("username")
    private String username;

    // 用户全局名
    @JsonProperty("global_name")
    private Object globalName;

    // 用户展示名
    @JsonProperty("display_name")
    private Object displayName;

    // 用户头像
    @JsonProperty("avatar")
    private String avatar;

    // 用户头像装饰
    @JsonProperty("avatar_decoration")
    private Object avatarDecoration;

    // 用户名后面的数字标记
    @JsonProperty("discriminator")
    private String discriminator;

    // 用户公共标记
    @JsonProperty("public_flags")
    private int publicFlags;

    // 是否为机器人
    @JsonProperty("bot")
    private boolean bot;
}
