package com.gpt.chatproject.service;

import com.github.binarywang.wxpay.bean.order.WxPayMpOrderResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.gpt.chatproject.entity.MembershipPricing;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MyWxPayService {

    WxPayMpOrderResult getOrderInfo(String orderName, String createIp, String openId, Integer membershipPricingId, String notifyUrl) throws WxPayException;

    List<MembershipPricing> getMembershipPricingList(String openid);

    String getAgreement();

    void payNotify(String xmlData) throws WxPayException;

    String getMemberStatus(String openid);
}
