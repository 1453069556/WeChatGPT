package com.gpt.chatproject.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("membership_pricing")
public class MembershipPricing {
    @TableId(value = "id")
    private int id;
    @TableField(value = "date")
    private int date;
    //分
    @TableField(value = "price")
    private BigDecimal price;
    @TableField(value = "oPrice")
    private BigDecimal oPrice;
    @TableField(value = "discounts")
    private String discounts;
    @TableField(value = "image_num")
    private int imageNum;
}
