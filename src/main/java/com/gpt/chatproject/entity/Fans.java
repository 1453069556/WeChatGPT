package com.gpt.chatproject.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("fans")
public class Fans {
    @TableId(value = "user_id")
    private String userId;
    @TableField(value = "subscribe_time")
    private LocalDateTime subscribeTime;
    @TableField(value = "unsubscribe_time")
    private LocalDateTime unsubscribeTime;
    @TableField(value = "expiration_time")
    private LocalDateTime expirationTime;
    @TableField(value = "inviter_id")
    private String inviterId;
    @TableField(value = "member_level")
    private Integer memberLevel;
}
