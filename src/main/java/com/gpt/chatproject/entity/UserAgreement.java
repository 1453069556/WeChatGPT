package com.gpt.chatproject.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user_agreement")
public class UserAgreement {
    @TableId(value = "id")
    private int id;
    @TableField(value = "svc_agmt")
    private String svcAgmt;
}
