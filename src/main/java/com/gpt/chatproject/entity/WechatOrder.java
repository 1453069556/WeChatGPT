package com.gpt.chatproject.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("wechat_order")
public class WechatOrder {
    private static final long serialVersionUID = 1L;
    @TableId(value = "order_id")
    private String orderId;
    // openid
    @TableField(value = "user_id")
    private String userId;
    @TableField(value = "order_name")
    private String orderName;
    @TableField(value = "membership_pricing_id")
    private Integer membershipPricingId;
    @TableField(value = "out_trade_no")
    private String outTradeNo;
    @TableField(value = "transaction_id")
    private String transactionId;
    @TableField(value = "total_fee")
    private BigDecimal totalFee;
    // ENUM('待支付', '已支付', '已取消')
    @TableField(value = "order_status")
    private String orderStatus;
    @TableField(value = "create_time")
    private String createTime;
    @TableField(value = "pay_time")
    private String payTime;
    @TableField(value = "cancel_time")
    private String cancelTime;

}
