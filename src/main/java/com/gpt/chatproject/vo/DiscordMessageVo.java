package com.gpt.chatproject.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@Data
public class DiscordMessageVo implements Serializable {

    @JsonProperty("id")
    private String id;
    @JsonProperty("type")
    private Integer type;
    @JsonProperty("content")
    private String content;
    @JsonProperty("channel_id")
    private String channelId;
    @JsonProperty("author")
    private AuthorDTO author;
    @JsonProperty("attachments")
    private List<ReferencedMessageDTO.AttachmentsDTO> attachments;
    @JsonProperty("embeds")
    private List<EmbedsDTO> embeds;
    @JsonProperty("mentions")
    private List<MentionsDTO> mentions;
    @JsonProperty("mention_roles")
    private List<?> mentionRoles;
    @JsonProperty("pinned")
    private Boolean pinned;
    @JsonProperty("mention_everyone")
    private Boolean mentionEveryone;
    @JsonProperty("tts")
    private Boolean tts;
    @JsonProperty("timestamp")
    private String timestamp;
    @JsonProperty("edited_timestamp")
    private Object editedTimestamp;
    @JsonProperty("flags")
    private Integer flags;
    @JsonProperty("components")
    private List<?> components;
    @JsonProperty("application_id")
    private String applicationId;
    @JsonProperty("interaction")
    private InteractionDTO interaction;
    @JsonProperty("webhook_id")
    private String webhookId;
    @JsonProperty("message_reference")
    @JsonIgnoreProperties(ignoreUnknown = true)
    private MessageReferenceDTO messageReference;
    @JsonProperty("referenced_message")
    @JsonIgnoreProperties(ignoreUnknown = true)
    private ReferencedMessageDTO referencedMessage;

    @NoArgsConstructor
    @Data
    public static class AuthorDTO {
        @JsonProperty("id")
        private String id;
        @JsonProperty("username")
        private String username;
        @JsonProperty("global_name")
        private Object globalName;
        @JsonProperty("display_name")
        private Object displayName;
        @JsonProperty("avatar")
        private String avatar;
        @JsonProperty("avatar_decoration")
        private Object avatarDecoration;
        @JsonProperty("discriminator")
        private String discriminator;
        @JsonProperty("public_flags")
        private Integer publicFlags;
        @JsonProperty("bot")
        private Boolean bot;
    }

    @NoArgsConstructor
    @Data
    public static class InteractionDTO {
        @JsonProperty("id")
        private String id;
        @JsonProperty("type")
        private Integer type;
        @JsonProperty("name")
        private String name;
        @JsonProperty("user")
        private UserDTO user;

        @NoArgsConstructor
        @Data
        public static class UserDTO {
            @JsonProperty("id")
            private String id;
            @JsonProperty("username")
            private String username;
            @JsonProperty("global_name")
            private Object globalName;
            @JsonProperty("display_name")
            private Object displayName;
            @JsonProperty("avatar")
            private String avatar;
            @JsonProperty("avatar_decoration")
            private Object avatarDecoration;
            @JsonProperty("discriminator")
            private String discriminator;
            @JsonProperty("public_flags")
            private Integer publicFlags;
        }
    }

    @NoArgsConstructor
    @Data
    public static class MessageReferenceDTO {
        @JsonProperty("channel_id")
        private String channelId;
        @JsonProperty("guild_id")
        private String guildId;
        @JsonProperty("message_id")
        private String messageId;
    }

    @NoArgsConstructor
    @Data
    public static class ReferencedMessageDTO {
        @JsonProperty("id")
        private String id;
        @JsonProperty("type")
        private Integer type;
        @JsonProperty("content")
        private String content;
        @JsonProperty("channel_id")
        private String channelId;
        @JsonProperty("author")
        private AuthorDTO author;
        @JsonProperty("attachments")
        private List<AttachmentsDTO> attachments;
        @JsonProperty("embeds")
        private List<?> embeds;
        @JsonProperty("mentions")
        private List<MentionsDTO> mentions;
        @JsonProperty("mention_roles")
        private List<?> mentionRoles;
        @JsonProperty("pinned")
        private Boolean pinned;
        @JsonProperty("mention_everyone")
        private Boolean mentionEveryone;
        @JsonProperty("tts")
        private Boolean tts;
        @JsonProperty("timestamp")
        private String timestamp;
        @JsonProperty("edited_timestamp")
        private Object editedTimestamp;
        @JsonProperty("flags")
        private Integer flags;
        @JsonProperty("components")
        private List<ComponentsDTO> components;

        @NoArgsConstructor
        @Data
        public static class AuthorDTO {
            @JsonProperty("id")
            private String id;
            @JsonProperty("username")
            private String username;
            @JsonProperty("global_name")
            private Object globalName;
            @JsonProperty("display_name")
            private Object displayName;
            @JsonProperty("avatar")
            private String avatar;
            @JsonProperty("avatar_decoration")
            private Object avatarDecoration;
            @JsonProperty("discriminator")
            private String discriminator;
            @JsonProperty("public_flags")
            private Integer publicFlags;
            @JsonProperty("bot")
            private Boolean bot;
        }

        @NoArgsConstructor
        @Data
        public static class AttachmentsDTO {
            @JsonProperty("id")
            private String id;
            @JsonProperty("filename")
            private String filename;
            @JsonProperty("size")
            private Integer size;
            @JsonProperty("url")
            private String url;
            @JsonProperty("proxy_url")
            private String proxyUrl;
            @JsonProperty("width")
            private Integer width;
            @JsonProperty("height")
            private Integer height;
            @JsonProperty("content_type")
            private String contentType;
        }

        @NoArgsConstructor
        @Data
        public static class MentionsDTO {
            @JsonProperty("id")
            private String id;
            @JsonProperty("username")
            private String username;
            @JsonProperty("global_name")
            private Object globalName;
            @JsonProperty("display_name")
            private Object displayName;
            @JsonProperty("avatar")
            private String avatar;
            @JsonProperty("avatar_decoration")
            private Object avatarDecoration;
            @JsonProperty("discriminator")
            private String discriminator;
            @JsonProperty("public_flags")
            private Integer publicFlags;
        }

        @NoArgsConstructor
        @Data
        public static class ComponentsDTO {
            @JsonProperty("type")
            private Integer type;
            @JsonProperty("components")
            private List<ComponentsDTOInner> components;

            @NoArgsConstructor
            @Data
            public static class ComponentsDTOInner {
                @JsonProperty("type")
                private Integer type;
                @JsonProperty("style")
                private Integer style;
                @JsonProperty("label")
                private String label;
                @JsonProperty("custom_id")
                private String customId;
                @JsonProperty("emoji")
                private EmojiDTO emoji;

                @NoArgsConstructor
                @Data
                public static class EmojiDTO {
                    @JsonProperty("name")
                    private String name;
                }
            }
        }
    }

    @NoArgsConstructor
    @Data
    public static class EmbedsDTO {
        @JsonProperty("type")
        private String type;
        @JsonProperty("url")
        private String url;
        @JsonProperty("thumbnail")
        private ThumbnailDTO thumbnail;

        @NoArgsConstructor
        @Data
        public static class ThumbnailDTO {
            @JsonProperty("url")
            private String url;
            @JsonProperty("proxy_url")
            private String proxyUrl;
            @JsonProperty("width")
            private Integer width;
            @JsonProperty("height")
            private Integer height;
        }
    }

    @NoArgsConstructor
    @Data
    public static class MentionsDTO {
        @JsonProperty("id")
        private String id;
        @JsonProperty("username")
        private String username;
        @JsonProperty("global_name")
        private Object globalName;
        @JsonProperty("display_name")
        private Object displayName;
        @JsonProperty("avatar")
        private String avatar;
        @JsonProperty("avatar_decoration")
        private Object avatarDecoration;
        @JsonProperty("discriminator")
        private String discriminator;
        @JsonProperty("public_flags")
        private Integer publicFlags;
    }
}