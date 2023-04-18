package com.gpt.chatproject.service;

import com.github.binarywang.wxpay.bean.order.WxPayMpOrderResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import org.springframework.stereotype.Service;

@Service
public interface MyWxPayService {

    WxPayMpOrderResult getOrderInfo(String orderName, String createIp, String openId, Integer amount) throws WxPayException;
}
