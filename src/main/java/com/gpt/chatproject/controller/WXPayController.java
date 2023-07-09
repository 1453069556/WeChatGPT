package com.gpt.chatproject.controller;

import com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse;
import com.github.binarywang.wxpay.bean.order.WxPayMpOrderResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.gpt.chatproject.entity.MembershipPricing;
import com.gpt.chatproject.service.MyWxPayService;
import com.gpt.chatproject.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
@Slf4j
@RequestMapping("/pay")
public class WXPayController {
    @Autowired
    private MyWxPayService myWxPayService;
    @Autowired
    private WxMpService wxMpService;
    @Value("${wxchat.notify_url}")
    private String NOTIFY_URL;
    @Value("${wxchat.url}")
    private String URL;

    @GetMapping("/authCallback")
    public String authCallback(@RequestParam("code") String code, Model model) {
        try {
            // 配置项
            WxOAuth2AccessToken accessToken = wxMpService.getOAuth2Service().getAccessToken(code);
            // 获取用户的OpenID
            String openid = accessToken.getOpenId();
            WxJsapiSignature jsapiSignature = wxMpService.createJsapiSignature(
                    URL + "pay/authCallback?code=" + code + "&state=");
            String appId = jsapiSignature.getAppId();
            long timestamp = jsapiSignature.getTimestamp();
            String nonceStr = jsapiSignature.getNonceStr();
            String signature = jsapiSignature.getSignature();
            // 将OpenID存储到js中
            model.addAttribute("openid", openid);
            model.addAttribute("name", myWxPayService.getMemberStatus(openid));
            List<MembershipPricing> membershipPricingList = myWxPayService.getMembershipPricingList(openid);
            String membershipPricingJson = JsonUtils.toJson(membershipPricingList);
            model.addAttribute("MembershipPricing", membershipPricingJson);
            model.addAttribute("appId", appId);
            model.addAttribute("timestamp", timestamp);
            model.addAttribute("nonceStr", nonceStr);
            model.addAttribute("signature", signature);
            Optional<MembershipPricing> minMembershipOptional = membershipPricingList.stream()
                    .min(Comparator.comparingInt(MembershipPricing::getId));
            if (minMembershipOptional.isPresent()) {
                MembershipPricing minMembership = minMembershipOptional.get();
                model.addAttribute("active", minMembership.getId());
                model.addAttribute("imageNum", minMembership.getImageNum());
            } else {
                model.addAttribute("active", 1);
                model.addAttribute("imageNum", 30);
            }
            // 重定向到支付页面，前端可以通过Ajax调用/getWechatPayParams接口发起支付
            return "view/payment";
        } catch (Exception e) {
            log.info(e.getMessage());
            e.printStackTrace();
            return "auth failed";
        }
    }

    @PostMapping(value = "/getWechatPayParams", produces = "application/json")
    @ResponseBody
    public String getWechatPayParams(@RequestParam("id") Integer MembershipPricingId,
                                     @RequestParam("openid") String openid,
                                     @RequestParam("dur") String dur,
                                     HttpServletRequest request) throws WxPayException {
        String remoteAddr = request.getRemoteAddr();
        WxPayMpOrderResult orderInfo = myWxPayService.getOrderInfo("小C会员-" + dur + "天",
                remoteAddr, openid, MembershipPricingId, NOTIFY_URL);
        return JsonUtils.toJson(orderInfo);
    }

    @PostMapping("/notify/order")
    @ResponseBody
    public String parseOrderNotifyResult(@RequestBody String xmlData) {
        try {
            myWxPayService.payNotify(xmlData);
        } catch (WxPayException e) {
            log.info(e.getMessage());
            return WxPayNotifyResponse.fail(e.getMessage());
        }
        return WxPayNotifyResponse.success("OK");
    }

}
