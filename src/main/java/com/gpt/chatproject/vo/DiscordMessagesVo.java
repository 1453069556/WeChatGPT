package com.gpt.chatproject.vo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscordMessagesVo implements Serializable {
    // 消息 ID
    @JsonProperty("id")
    private String id;

    // 消息类型
    @JsonProperty("type")
    private int type;

    // 消息内容
    @JsonProperty("content")
    private String content;

    // 消息所在频道 ID
    @JsonProperty("channel_id")
    private String channelId;

    // 消息发送者信息
    @JsonProperty("author")
    private DiscordUserVo author;

    // 消息附件列表
    @JsonProperty("attachments")
    private List<DiscordAttachmentVo> attachments;

    // 消息嵌入内容列表
    @JsonProperty("embeds")
    private List<Object> embeds;

    // 提到的用户列表
    @JsonProperty("mentions")
    private List<DiscordUserVo> mentions;

    // 提到的角色列表
    @JsonProperty("mention_roles")
    private List<Object> mentionRoles;

    // 是否被置顶
    @JsonProperty("pinned")
    private boolean pinned;

    // 是否提到了 @everyone
    @JsonProperty("mention_everyone")
    private boolean mentionEveryone;

    // 是否使用了语音朗读功能
    @JsonProperty("tts")
    private boolean tts;

    // 消息创建时间
    @JsonProperty("timestamp")
    private String timestamp;

    // 消息编辑时间
    @JsonProperty("edited_timestamp")
    private String editedTimestamp;

    // 消息标记
    @JsonProperty("flags")
    private int flags;

    // 消息组件列表
    @JsonProperty("components")
    private List<DiscordComponentVo> components;

    // 引用的消息信息
    @JsonProperty("message_reference")
    private DiscordMessageReferenceVo messageReference;

    // 被引用的消息信息
    @JsonProperty("referenced_message")
    private DiscordReferencedMessageVo referencedMessage;
}
