package com.gpt.chatproject.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 组件类，用于表示一个组件，包括组件类型和组件内部的其他属性
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscordComponentVo {

    // 组件类型，对应的是Discord API中的组件类型 */
    private int type;

    // 组件内部的其他属性
    private List<InnerComponent> components;

    // 内部组件类，用于表示组件内部的具体属性
    @Data
    public static class InnerComponent {

        /** 组件类型，对应的是Discord API中的组件类型 */
        private int type;

        /** 组件样式，对应的是Discord API中的组件样式 */
        private int style;

        /** 组件的标签，通常是一个文本标签 */
        private String label;

        /** 组件的表情符号 */
        private Emoji emoji;

        /** 组件的自定义ID，用于识别组件 */
        @JsonProperty("custom_id")
        private String customId;

        /** 组件的链接地址 */
        private String url;
    }


    // 表情符号类，用于表示组件内部的表情符号
    @Data
    public static class Emoji {

        /** 表情符号的名称 */
        private String name;
    }

}