package com.gpt.chatproject.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class DiscordHttpCustomVo implements Serializable {
    @JsonProperty("type")
    private int type;

    @JsonProperty("nonce")
    private String nonce;

    @JsonProperty("guild_id")
    private String guildId;

    @JsonProperty("channel_id")
    private String channelId;

    @JsonProperty("message_flags")
    private int messageFlags;

    @JsonProperty("message_id")
    private String messageId;

    @JsonProperty("application_id")
    private String applicationId;

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("data")
    private InteractionData data;

    @Data
    @NoArgsConstructor
    public static class InteractionData {

        @JsonProperty("component_type")
        private int componentType;

        @JsonProperty("custom_id")
        private String customId;
    }
}
