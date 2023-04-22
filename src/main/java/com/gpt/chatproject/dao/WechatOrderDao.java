package com.gpt.chatproject.dao;

import com.gpt.chatproject.entity.WechatOrder;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WechatOrderDao {
    int insert(WechatOrder wechatOrder);

    int update(WechatOrder wechatOrder);

    int delete(Integer orderId);

    WechatOrder findById(Integer orderId);

    List<WechatOrder> findAll();

    List<WechatOrder> findByUserId(String userId);


    WechatOrder findByOutTradeNo(String outTradeNo);
}
