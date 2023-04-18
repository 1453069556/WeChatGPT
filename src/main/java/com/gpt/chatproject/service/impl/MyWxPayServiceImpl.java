package com.gpt.chatproject.service.impl;

import com.github.binarywang.wxpay.bean.order.WxPayMpOrderResult;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.gpt.chatproject.service.MyWxPayService;
import com.gpt.chatproject.utils.MyDateUtils;
import com.gpt.chatproject.utils.MyStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;

@Service
public class MyWxPayServiceImpl implements MyWxPayService {
    @Autowired
    private com.github.binarywang.wxpay.service.WxPayService wxPayService;

    @Override
    public WxPayMpOrderResult getOrderInfo(String orderName, String createIp, String openId, Integer amount) throws WxPayException {
        WxPayUnifiedOrderRequest orderRequest = new WxPayUnifiedOrderRequest();
        orderRequest.setBody(orderName);
        orderRequest.setOutTradeNo(MyStringUtils.generateRandomString(32));
        orderRequest.setTotalFee(amount * 100);//分
        orderRequest.setTradeType(WxPayConstants.TradeType.JSAPI);
        // TODO 换自己的回调网址
        orderRequest.setNotifyUrl("https://www.weixin.qq.com/wxpay/pay.php");
        orderRequest.setOpenid(openId);
        orderRequest.setSpbillCreateIp(createIp);
        orderRequest.setTimeStart(MyDateUtils.getBeijingTime("yyyyMMddHHmmss"));
        orderRequest.setTimeExpire(MyDateUtils.dateFormat(MyDateUtils.add(Calendar.MINUTE, 5), "yyyyMMddHHmmss"));
        return wxPayService.createOrder(orderRequest);
    }
}
