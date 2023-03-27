package com.gpt.chatproject;

import com.gpt.chatproject.dao.FansDao;
import com.gpt.chatproject.entity.Fans;
import com.gpt.chatproject.utils.RedisUtils;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.menu.WxMenu;
import me.chanjar.weixin.common.bean.menu.WxMenuButton;
import me.chanjar.weixin.common.enums.TicketType;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpQrcodeService;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;


@SpringBootTest
class ChatProjectApplicationTests {
    @Autowired
    private WxMpService wxService;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private FansDao fansDao;
    @Test
    public void getMenu() throws WxErrorException {
        WxMenu menu = new WxMenu();
        // 提示用的
        WxMenuButton tips = new WxMenuButton();
        tips.setName("小C没答完的处理方式");
        tips.setType(WxConsts.MenuButtonType.CLICK);
        tips.setKey("TIPS");
        // 菜单按钮
        WxMenuButton menuButton = new WxMenuButton();
        menuButton.setName("菜单");
        // 生成群邀请二维码按钮
        WxMenuButton menuButton1 = new WxMenuButton();
        menuButton1.setType(WxConsts.MenuButtonType.CLICK);
        menuButton1.setName("添加客服微信进群");
        menuButton1.setKey("JOIN_GROUP_POST");
        menuButton.getSubButtons().add(menuButton1);
        menu.getButtons().add(menuButton);
        menu.getButtons().add(tips);
        this.wxService.getMenuService().menuCreate(menu);
    }


//    @Test
//    public void testCreat() throws WxErrorException{
//        // 二维码服务
//        WxMpQrcodeService qrcodeService = wxService.getQrcodeService();
//        String s = UUID.randomUUID().toString();
//        // 获取永久二维码ticket
//        WxMpQrCodeTicket wxMpQrCodeTicket = qrcodeService.qrCodeCreateLastTicket(s);
//        // 获取图片二维码
//        File file = qrcodeService.qrCodePicture(wxMpQrCodeTicket);
//        System.out.println(wxMpQrCodeTicket);
//    }

}
