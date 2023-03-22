package com.gpt.chatproject;

import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.menu.WxMenu;
import me.chanjar.weixin.common.bean.menu.WxMenuButton;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class ChatProjectApplicationTests {
    @Autowired
    private WxMpService wxService;
    @Test
    public void getMenu() throws WxErrorException {
        WxMenu menu = new WxMenu();
        // 提示用的
        WxMenuButton tips = new WxMenuButton();
        tips.setName("点击小键盘聊天噢~");
        tips.setType(WxConsts.MenuButtonType.CLICK);
        tips.setKey("TIPS");
        // 菜单按钮
        WxMenuButton menuButton = new WxMenuButton();
        menuButton.setName("菜单");
        // 生成群邀请二维码按钮
        WxMenuButton menuButton1 = new WxMenuButton();
        menuButton1.setType(WxConsts.MenuButtonType.CLICK);
        menuButton1.setName("生成群二维码");
        menuButton1.setKey("JOIN_GROUP_POST");
        menuButton.getSubButtons().add(menuButton1);
        menu.getButtons().add(menuButton);
        menu.getButtons().add(tips);
        this.wxService.getMenuService().menuCreate(menu);
    }

}
