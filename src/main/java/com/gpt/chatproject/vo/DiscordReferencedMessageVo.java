package com.gpt.chatproject.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscordReferencedMessageVo {
    /**
     * 引用的消息ID
     */
    @JsonProperty("id")
    private String id;

    /**
     * 引用消息的类型。值为 0 表示默认，值为 19 表示消息是一个嵌入式消息。
     */
    @JsonProperty("type")
    private int type;

    /**
     * 引用的消息内容
     */
    @JsonProperty("content")
    private String content;

    /**
     * 引用的消息所在的频道ID
     */
    @JsonProperty("channel_id")
    private String channelId;

    /**
     * 引用消息的作者
     */
    @JsonProperty("author")
    private DiscordUserVo author;
}
