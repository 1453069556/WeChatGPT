package com.gpt.chatproject.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "midjourney")
public class MidjourneyConfig {
    private String authorization;
    private String applicationId;
    private String guildId;
    private String channelId;
    private String sessionId;
    private Integer messagesLimit;
}
