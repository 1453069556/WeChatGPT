package com.gpt.chatproject.config;

import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.menu.WxMenu;
import me.chanjar.weixin.common.bean.menu.WxMenuButton;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WxButtonConfig {
    @Autowired
    private WxMpService wxService;
    @Value("${wxchat.vip_url}")
    private String VIP_URL;
    @Bean
    public void buttonConfig() throws WxErrorException {
        WxMenu menu = new WxMenu();
        // 提示用的
        WxMenuButton tips = new WxMenuButton();
        tips.setName("默认聊天模式");
        tips.setType(WxConsts.MenuButtonType.CLICK);
        tips.setKey("RESET_CHAT_TYPE");
        // 菜单按钮
        WxMenuButton menuButton = new WxMenuButton();
        menuButton.setName("菜单");
        // 会员按钮-点击进入会员开通页
        WxMenuButton myVipButton = new WxMenuButton();
        myVipButton.setType(WxConsts.MenuButtonType.VIEW);
        myVipButton.setName("开通小C会员");
        myVipButton.setUrl(VIP_URL);
        menuButton.getSubButtons().add(myVipButton);
        // 查询会员次数按钮-点击获取绘图剩余次数
        WxMenuButton getImageNumButton = new WxMenuButton();
        getImageNumButton.setType(WxConsts.MenuButtonType.CLICK);
        getImageNumButton.setName("获取会员信息");
        getImageNumButton.setKey("GET_MEMBER_INFO");
        menuButton.getSubButtons().add(getImageNumButton);
        // 菜单按钮-生成群邀请二维码按钮
        WxMenuButton menuButton1 = new WxMenuButton();
        menuButton1.setType(WxConsts.MenuButtonType.CLICK);
        menuButton1.setName("添加客服微信");
        menuButton1.setKey("JOIN_GROUP_POST");
        menuButton.getSubButtons().add(menuButton1);
        // 绘图菜单
        WxMenuButton FunctionBox = new WxMenuButton();
        FunctionBox.setName("小C功能箱");
        // 绘图菜单-进入dall绘图聊天模式
        WxMenuButton FunctionBox2 = new WxMenuButton();
        FunctionBox2.setType(WxConsts.MenuButtonType.CLICK);
        FunctionBox2.setName("小C画廊-娱乐版");
        FunctionBox2.setKey("AI_IMAGE_CHAT_DALL");
        FunctionBox.getSubButtons().add(FunctionBox2);
        // 绘图菜单-进入Midjourney绘图聊天模式
        WxMenuButton FunctionBox3 = new WxMenuButton();
        FunctionBox3.setType(WxConsts.MenuButtonType.CLICK);
        FunctionBox3.setName("小C画廊-Midjourney");
        FunctionBox3.setKey("AI_IMAGE_CHAT_MIDJOURNEY");
        FunctionBox.getSubButtons().add(FunctionBox3);

        // 菜单添加并提交
        menu.getButtons().add(menuButton);
        menu.getButtons().add(FunctionBox);
        menu.getButtons().add(tips);
        this.wxService.getMenuService().menuCreate(menu);
    }
}
