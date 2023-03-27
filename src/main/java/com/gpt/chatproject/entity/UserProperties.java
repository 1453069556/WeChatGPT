package com.gpt.chatproject.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("user_properties")
public class UserProperties {
    @TableId(value = "user_id")
    private String userId;
    @TableField(value = "invited_count")
    private Integer invitedCount;
    @TableField(value = "consumption_total")
    private BigDecimal consumptionTotal;
    private BigDecimal balance;
    private Integer points;
}