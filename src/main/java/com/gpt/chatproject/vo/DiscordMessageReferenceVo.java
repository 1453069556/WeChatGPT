package com.gpt.chatproject.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscordMessageReferenceVo implements Serializable {
    /**
     * 引用的消息所在的频道ID
     */
    @JsonProperty("channel_id")
    private String channelId;

    /**
     * 引用的消息所在的服务器ID
     */
    @JsonProperty("guild_id")
    private String guildId;

    /**
     * 引用的消息ID
     */
    @JsonProperty("message_id")
    private String messageId;
}