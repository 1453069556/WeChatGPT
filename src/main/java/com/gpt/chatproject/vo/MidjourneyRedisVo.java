package com.gpt.chatproject.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MidjourneyRedisVo implements Serializable {
    private String userId;
    private String url;
    private String prompt;
    private long messageId;
    private String discordMessageId;
    private List<String> attachmentsIds;
    private List<String> customs;

    public MidjourneyRedisVo(String userId) {
        this.userId = userId;
    }

}
