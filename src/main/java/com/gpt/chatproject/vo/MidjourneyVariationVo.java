package com.gpt.chatproject.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MidjourneyVariationVo implements Serializable {
    private String userId;
    private String url;
    private String prompt;

    public MidjourneyVariationVo(String userId) {
        this.userId = userId;
    }

}
