package com.gpt.chatproject.controller;

import com.github.binarywang.wxpay.bean.order.WxPayMpOrderResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.gpt.chatproject.service.MyWxPayService;
import com.gpt.chatproject.utils.JsonUtils;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/pay")
public class WXPayController {
    @Autowired
    private MyWxPayService myWxPayService;
    @Autowired
    private WxMpService wxMpService;

    @GetMapping("/authCallback")
    public String authCallback(@RequestParam("code") String code, Model model) {
        try {
            // 配置项
            WxOAuth2AccessToken accessToken = wxMpService.getOAuth2Service().getAccessToken(code);
            WxJsapiSignature jsapiSignature = wxMpService.createJsapiSignature("https://zhixinyun.work/pay/authCallback?code=" + code + "&state=");
            String appId = jsapiSignature.getAppId();
            long timestamp = jsapiSignature.getTimestamp();
            String nonceStr = jsapiSignature.getNonceStr();
            String signature = jsapiSignature.getSignature();
            model.addAttribute("appId", appId);
            model.addAttribute("timestamp", timestamp);
            model.addAttribute("nonceStr", nonceStr);
            model.addAttribute("signature", signature);

            // 获取用户的OpenID
            String openid = accessToken.getOpenId();
            // 将OpenID存储到js中
            model.addAttribute("openid", openid);
            model.addAttribute("product_name", "小C会员开通");
            // 重定向到支付页面，前端可以通过Ajax调用/getWechatPayParams接口发起支付
            return "view/payment";
        } catch (Exception e) {
            e.printStackTrace();
            return "auth failed";
        }
    }


    @PostMapping(value = "/getWechatPayParams", produces = "application/json")
    @ResponseBody
    public String getWechatPayParams(@RequestParam("amount") Integer amount,
                                     @RequestParam("openid") String openid,
                                     HttpServletRequest request) throws WxPayException {
        String remoteAddr = request.getRemoteAddr();
        WxPayMpOrderResult orderInfo = myWxPayService.getOrderInfo("小C会员开通", remoteAddr, openid, amount);
        return JsonUtils.toJson(orderInfo);
    }


}
