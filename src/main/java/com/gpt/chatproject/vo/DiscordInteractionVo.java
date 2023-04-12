package com.gpt.chatproject.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscordInteractionVo implements Serializable {

    private static final long serialVersionUID = 1L;

    // 类型，2表示接收到的交互请求
    @JsonProperty("type")
    private int type;

    // 应用程序ID
    @JsonProperty("application_id")
    private String applicationId;

    // 服务器ID
    @JsonProperty("guild_id")
    private String guildId;

    // 频道ID
    @JsonProperty("channel_id")
    private String channelId;

    // 会话ID
    @JsonProperty("session_id")
    private String sessionId;

    // 请求数据
    @JsonProperty("data")
    private InteractionData data;

    // 随机数
    @JsonProperty("nonce")
    private String nonce;

    // 请求数据实体类
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InteractionData implements Serializable {

        private static final long serialVersionUID = 1L;

        // 数据版本
        @JsonProperty("version")
        private String version;

        // 数据ID
        @JsonProperty("id")
        private String id;

        // 数据名称
        @JsonProperty("name")
        private String name;

        // 数据类型，1表示命令
        @JsonProperty("type")
        private int type;

        // 数据选项列表
        @JsonProperty("options")
        private List<Option> options;

        // 应用程序命令实体类
        @JsonProperty("application_command")
        private ApplicationCommand applicationCommand;

        // 附件列表
        @JsonProperty("attachments")
        private List<String> attachments;

        // 数据选项实体类
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Option implements Serializable {

            private static final long serialVersionUID = 1L;

            // 选项类型，3表示字符串
            @JsonProperty("type")
            private int type;

            // 选项名称
            @JsonProperty("name")
            private String name;

            // 选项值
            @JsonProperty("value")
            private String value;
        }

        // 应用程序命令实体类
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ApplicationCommand implements Serializable {

            private static final long serialVersionUID = 1L;

            // 命令ID
            @JsonProperty("id")
            private String id;

            // 应用程序ID
            @JsonProperty("application_id")
            private String applicationId;

            // 命令版本
            @JsonProperty("version")
            private String version;

            // 默认成员权限
            @JsonProperty("default_member_permissions")
            private List<String> defaultMemberPermissions;

            // 命令类型，1表示聊天命令
            @JsonProperty("type")
            private int type;

            // 是否为不安全内容
            @JsonProperty("nsfw")
            private boolean nsfw;

            // 命令名称
            @JsonProperty("name")
            private String name;

            // 命令描述
            @JsonProperty("description")
            private String description;

            // 是否允许在私聊中使用
            @JsonProperty("dm_permission")
            private boolean dmPermission;

            // 选项
            @JsonProperty("options")
            private List<Option> options;


            // 应用程序命令实体类
            @Data
            @Builder
            @NoArgsConstructor
            @AllArgsConstructor
            public static class Option implements Serializable {
                private int type;
                private String name;
                private String description;
                private boolean required;
            }
        }
    }
}