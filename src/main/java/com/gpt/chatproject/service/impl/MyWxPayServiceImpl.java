package com.gpt.chatproject.service.impl;

import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.order.WxPayMpOrderResult;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.gpt.chatproject.dao.MemberInfoDao;
import com.gpt.chatproject.dao.MembershipPricingDao;
import com.gpt.chatproject.dao.UserAgreementDao;
import com.gpt.chatproject.dao.WechatOrderDao;
import com.gpt.chatproject.entity.MemberInfo;
import com.gpt.chatproject.entity.MembershipPricing;
import com.gpt.chatproject.entity.UserAgreement;
import com.gpt.chatproject.entity.WechatOrder;
import com.gpt.chatproject.enums.MemberLevel;
import com.gpt.chatproject.enums.OrderStatus;
import com.gpt.chatproject.service.MyWxPayService;
import com.gpt.chatproject.utils.MyDateUtils;
import com.gpt.chatproject.utils.MyStringUtils;
import com.gpt.chatproject.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class MyWxPayServiceImpl implements MyWxPayService {
    @Autowired
    private com.github.binarywang.wxpay.service.WxPayService wxPayService;

    @Autowired
    private MembershipPricingDao membershipPricingDao;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private UserAgreementDao userAgreementDao;

    @Autowired
    private WechatOrderDao wechatOrderDao;

    @Autowired
    private MemberInfoDao memberInfoDao;

    @Override
    public WxPayMpOrderResult getOrderInfo(String orderName, String createIp, String openId, Integer membershipPricingId, String notifyUrl) throws WxPayException {
        // 创建订单
        WxPayUnifiedOrderRequest orderRequest = new WxPayUnifiedOrderRequest();
        String timeNow = MyDateUtils.getBeijingTime("yyyyMMddHHmmss");
        orderRequest.setOutTradeNo(membershipPricingId + MyDateUtils.getBeijingTime("yyyyMMddHHmmssSSS") +
                MyStringUtils.getRandomIntegerString(10));
        MembershipPricing membershipPricing = membershipPricingDao.findById(membershipPricingId);
        BigDecimal price = membershipPricing.getPrice();
        orderRequest.setBody(orderName);
        orderRequest.setTotalFee(price.multiply(new BigDecimal("100")).intValue());//分
        orderRequest.setTradeType(WxPayConstants.TradeType.JSAPI);
        orderRequest.setNotifyUrl(notifyUrl);
        orderRequest.setOpenid(openId);
        orderRequest.setSpbillCreateIp(createIp);
        orderRequest.setTimeStart(timeNow);
        orderRequest.setTimeExpire(MyDateUtils.dateFormat(MyDateUtils.add(Calendar.MINUTE, 5), "yyyyMMddHHmmss"));

        // 录入订单
        WechatOrder wechatOrder = new WechatOrder();
        wechatOrder.setUserId(openId);
        wechatOrder.setOrderName(orderName);
        wechatOrder.setMembershipPricingId(membershipPricingId);
        wechatOrder.setOutTradeNo(orderRequest.getOutTradeNo());
        wechatOrder.setTotalFee(price);
        wechatOrder.setOrderStatus(OrderStatus.PENDING.getType());
        wechatOrder.setCreateTime(timeNow);
        wechatOrderDao.insert(wechatOrder);

        return wxPayService.createOrder(orderRequest);
    }

    @Override
    public List<MembershipPricing> getMembershipPricingList(String openid) {
        MemberInfo byUserId = memberInfoDao.findByUserId(openid);
        List<MembershipPricing> all;
        if (byUserId != null) {
            all = membershipPricingDao.findExcludeFirstOrder();
        } else {
            all = membershipPricingDao.findAll();
        }
        return all;
    }

    @Override
    public String getAgreement() {
        List<UserAgreement> all = userAgreementDao.findAll();
        return all.get(0).getSvcAgmt();
    }

    @Override
    public void payNotify(String xmlData) {
        WxPayOrderNotifyResult result;
        try {
            result = wxPayService.parseOrderNotifyResult(xmlData);
            if (!WxPayConstants.ResultCode.SUCCESS.equals(result.getReturnCode())) {
                log.error(xmlData);
                throw new WxPayException("微信支付-通知失败！");
            }
            if (!WxPayConstants.ResultCode.SUCCESS.equals(result.getResultCode())) {
                log.error(xmlData);
                throw new WxPayException("微信支付-通知失败！");
            }

            // 支付成功业务处理
            // 订单状态更新
            WechatOrder wechatOrder = wechatOrderDao.findByOutTradeNo(result.getOutTradeNo());
            wechatOrder.setTransactionId(result.getTransactionId());
            wechatOrder.setPayTime(result.getTimeEnd());
            wechatOrder.setOrderStatus(OrderStatus.PAID.getType());
            wechatOrderDao.update(wechatOrder);
            // 添加会员信息
            Date beijingDate = MyDateUtils.getBeijingDate();
            MemberInfo byUserId = memberInfoDao.findByUserId(result.getOpenid());
            MembershipPricing membershipPricing = membershipPricingDao.findById(wechatOrder.getMembershipPricingId());
            if (byUserId == null) {
                MemberInfo memberInfo = new MemberInfo();
                memberInfo.setUserId(result.getOpenid());
                memberInfo.setMemberLevel(MemberLevel.REG.getType());
                memberInfo.setPoints(result.getTotalFee());
                memberInfo.setStartTime(result.getTimeEnd());
                Date add = MyDateUtils.add(beijingDate, Calendar.DATE, membershipPricing.getDate());
                memberInfo.setExpireTime(MyDateUtils.dateFormat(add, "yyyyMMddHHmmss"));
                memberInfoDao.insert(memberInfo);
            } else {
                // 在现有的时间基础上续时
                String expireTime = byUserId.getExpireTime();
                Date date = MyDateUtils.formatDate(expireTime, "yyyyMMddHHmmss");
                Date nowDate = MyDateUtils.getBeijingDate();
                Date newDate;
                if (nowDate.getTime() - date.getTime() > 0) {
                    newDate = MyDateUtils.add(nowDate, Calendar.DATE, membershipPricing.getDate());
                } else {
                    newDate = MyDateUtils.add(date, Calendar.DATE, membershipPricing.getDate());
                }
                byUserId.setExpireTime(MyDateUtils.dateFormat(newDate, "yyyyMMddHHmmss"));
                memberInfoDao.update(byUserId);
            }
            redisUtils.setMemberLevel(result.getOpenid(), MemberLevel.REG);
        } catch (WxPayException e) {
            log.error("微信支付-通知失败", e);
        }
    }

    @Override
    public String getMemberStatus(String openid) {
        // 获取到期时间并格式化
        MemberInfo byUserId = memberInfoDao.findByUserId(openid);
        Date beijingDate = MyDateUtils.getBeijingDate();
        if (byUserId == null) {
            return "未开通会员";
        } else if (
                MyDateUtils.formatDate(byUserId.getExpireTime(), "yyyyMMddHHmmss").getTime()
                        - beijingDate.getTime() < 0) {
            return "会员已到期";
        }
        Date formatDate = MyDateUtils.formatDate(byUserId.getExpireTime(), "yyyyMMddHHmmss");
        String dateFormat = MyDateUtils.dateFormat(formatDate, "yyyy-MM-dd HH:mm:ss");
        return "到期时间：" + dateFormat;
    }
}
