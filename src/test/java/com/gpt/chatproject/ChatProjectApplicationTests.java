package com.gpt.chatproject;

import com.gpt.chatproject.dao.FansDao;
import com.gpt.chatproject.enums.DallResponseType;
import com.gpt.chatproject.enums.DallSizeType;
import com.gpt.chatproject.utils.DallUtils;
import com.gpt.chatproject.utils.RedisUtils;
import com.theokanning.openai.image.Image;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.menu.WxMenu;
import me.chanjar.weixin.common.bean.menu.WxMenuButton;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
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
        tips.setName("默认聊天模式");
        tips.setType(WxConsts.MenuButtonType.CLICK);
        tips.setKey("RESET_CHAT_TYPE");
        // 菜单按钮
        WxMenuButton menuButton = new WxMenuButton();
        menuButton.setName("菜单");
        // 菜单按钮-生成群邀请二维码按钮
        WxMenuButton menuButton1 = new WxMenuButton();
        menuButton1.setType(WxConsts.MenuButtonType.CLICK);
        menuButton1.setName("添加客服微信进群");
        menuButton1.setKey("JOIN_GROUP_POST");
        menuButton.getSubButtons().add(menuButton1);
        // 绘图菜单
        WxMenuButton FunctionBox = new WxMenuButton();
        FunctionBox.setName("小C功能箱");
        // 绘图菜单-进入绘图聊天模式
        WxMenuButton FunctionBox2 = new WxMenuButton();
        FunctionBox2.setType(WxConsts.MenuButtonType.CLICK);
        FunctionBox2.setName("小C画廊");
        FunctionBox2.setKey("AI_IMAGE_CHAT");
        FunctionBox.getSubButtons().add(FunctionBox2);
        // 菜单添加并提交
        menu.getButtons().add(menuButton);
        menu.getButtons().add(FunctionBox);
        menu.getButtons().add(tips);
        this.wxService.getMenuService().menuCreate(menu);
    }

    @Autowired
    private DallUtils dallUtils;

    @Test
    public void testCreatImage() {
//        String prompt = "the shot of a robotic dinosaur [description], intrincated, metal, high resolution, CGSociety, ZBrushCentral, digital illustration, detailed background, 3d shading";
        String prompt = "An astronaut dressed in a space suit sits on a rock on the surface of the moon, holding a turkey in his hand and enjoying a Thanksgiving feast. The sky on the moon is dark with stars shining. Please use AI painting tools to create this image.";
        List<Image> images = dallUtils.dall2(prompt, 1, DallSizeType.LARGE, DallResponseType.B64_JSON);
        for (Image image : images) {
            byte[] bytes = Base64.getDecoder().decode(image.getB64Json());
            try {
                File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".jpg");
                Files.write(tempFile.toPath(), bytes);
                tempFile.deleteOnExit();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    @Test
    public void testCreatImage2() throws IOException {
        // 读取JPG文件
        BufferedImage imageLocal = ImageIO.read(new File("C:\\Users\\Ms Tong\\Desktop\\小c.jpg"));
        // 写入PNG文件
        ImageIO.write(imageLocal, "png", new File("C:\\Users\\Ms Tong\\Desktop\\小c.png"));
        List<Image> images = dallUtils.dall2(new File("C:\\Users\\Ms Tong\\Desktop\\小c.png"), 1, DallSizeType.LARGE, DallResponseType.B64_JSON);
        for (Image image : images) {
            byte[] bytes = Base64.getDecoder().decode(image.getB64Json());
            try {
                File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".jpg");
                Files.write(tempFile.toPath(), bytes);
                tempFile.deleteOnExit();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
