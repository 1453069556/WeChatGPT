package com.gpt.chatproject.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MidjourneyMqVo implements Serializable {
    private String fromUser;
    private long messageId;
    /**
     * 检测次数
     */
    private Integer checkCount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String url;


    /**
     * checkCount初始化为0
     *
     * @param fromUser  fromUser
     * @param messageId messageId
     */
    public MidjourneyMqVo(String fromUser, long messageId) {
        this.fromUser = fromUser;
        this.messageId = messageId;
        this.checkCount = 0;
        this.url = "null";
    }
}
