package com.gpt.chatproject.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("fans")
public class MemberInfo {
    private static final long serialVersionUID = 1L;

    @TableId(value = "user_id")
    private String userId;
    @TableField(value = "member_level")
    private String memberLevel;
    @TableField(value = "points")
    private Integer points;
    @TableField(value = "start_time")
    private String startTime;
    @TableField(value = "expire_time")
    private String expireTime;
}
