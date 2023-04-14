package com.gpt.chatproject;

import com.gpt.chatproject.config.MidjourneyConfig;
import com.gpt.chatproject.listener.MidjourneyMqListener;
import com.gpt.chatproject.utils.JsonUtils;
import com.gpt.chatproject.utils.MidjourneyUtils;
import com.gpt.chatproject.utils.RedisUtils;
import com.gpt.chatproject.utils.WeChatUtils;
import com.gpt.chatproject.vo.DiscordHttpMessageVo;
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
    @Autowired
    private RedisUtils redisUtils;

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
        // 绘图菜单-进入dall绘图聊天模式
        WxMenuButton FunctionBox2 = new WxMenuButton();
        FunctionBox2.setType(WxConsts.MenuButtonType.CLICK);
        FunctionBox2.setName("小C画廊-DALL");
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

    @Autowired
    private WeChatUtils weChatUtils;

    @Test
    public void testSentHref() throws WxErrorException {
        String authorization = midConfig.getAuthorization();
        String channelId = midConfig.getChannelId();
        String messages = MidjourneyUtils.getMessages(authorization, channelId, 50);
        DiscordHttpMessageVo[] midjourneyMqVos = JsonUtils.fromJsonArray(messages, DiscordHttpMessageVo.class);
        assert midjourneyMqVos != null;
        String hrefButton = MidjourneyMqListener.getSendOkMessage(midjourneyMqVos[0]);
        weChatUtils.sendKefuTextMessage("oKV5h5x1mFdgv3cuUmzMzXn56o8Y",hrefButton);
    }

    @Autowired
    private MidjourneyConfig midConfig;

//    @Test
//    public void testCustom() {
//        String customId = "MJ::JOB::variation::1::6bee8179-80f7-454d-83b7-1dbb89dc4d03";
//        String messageId = "";
//        DiscordHttpCustomVo customVo = MidjourneyUtils.getCustomVo(midConfig.getApplicationId(),
//                midConfig.getGuildId(), midConfig.getChannelId(), messageId, customId);
//        MidjourneyUtils.sendCustomCommand(midConfig.getAuthorization(), customVo);
//
////        String messages = MidjourneyUtils.getMessages(midConfig.getAuthorization(),
////                midConfig.getChannelId(),
////                midConfig.getMessagesLimit());
//        System.out.println(customVo);
//    }
//    @Autowired
//    private DallUtils dallUtils;
//
//    @Test
//    public void testCreatImage() {
////        String prompt = "the shot of a robotic dinosaur [description], intrincated, metal, high resolution, CGSociety, ZBrushCentral, digital illustration, detailed background, 3d shading";
//        String prompt = "An astronaut dressed in a space suit sits on a rock on the surface of the moon, holding a turkey in his hand and enjoying a Thanksgiving feast. The sky on the moon is dark with stars shining. Please use AI painting tools to create this image.";
//        List<Image> images = dallUtils.dall2(prompt, 1, DallSizeType.LARGE, DallResponseType.B64_JSON);
//        for (Image image : images) {
//            byte[] bytes = Base64.getDecoder().decode(image.getB64Json());
//            try {
//                File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".jpg");
//                Files.write(tempFile.toPath(), bytes);
//                tempFile.deleteOnExit();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
//    @Test
//    public void testCreatImage2() throws IOException {
//        // 读取JPG文件
//        BufferedImage imageLocal = ImageIO.read(new File("C:\\Users\\Ms Tong\\Desktop\\小c.jpg"));
//        // 写入PNG文件
//        ImageIO.write(imageLocal, "png", new File("C:\\Users\\Ms Tong\\Desktop\\小c.png"));
//        List<Image> images = dallUtils.dall2(new File("C:\\Users\\Ms Tong\\Desktop\\小c.png"), 1, DallSizeType.LARGE, DallResponseType.B64_JSON);
//        for (Image image : images) {
//            byte[] bytes = Base64.getDecoder().decode(image.getB64Json());
//            try {
//                File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".jpg");
//                Files.write(tempFile.toPath(), bytes);
//                tempFile.deleteOnExit();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
//    @Autowired
//    private MidjourneyUtils midjourneyUtils;

//    @Test
//    public void testJsoup() throws IOException, InterruptedException {
//        for (int i = 0; i < 4; i++) {
//            Thread.sleep(2000);
//            String authorization = "MTA4MTgwOTAzNzY1NTU1NjA5Ng.G052C-.KLdcVi-WToPmruyC19z0R6UDz4JDNGakVqXZ7g";
//            String applicationId = "936929561302675456";
//            String guildId = "1093749340285173810";
//            String channelId = "1093749643051020378";
//            long messageId = (long) (Math.random() * 999999999L);
//            // 创建 OkHttpClient 实例
//            DiscordHttpInteractionVo command = MidjourneyUtils.getCommand(applicationId, guildId, channelId, "cat", messageId);
//            boolean sendOk = midjourneyUtils.sendCommand(authorization, command);
//            if (sendOk) {
//                //TODO
//                if (i == 1){
//                    MidjourneyMqVo midjourneyCatchVo1 = new MidjourneyMqVo("oKV5h5x1mFdgv3cuUmzMzXn56o8Y", messageId);
//                    redisUtils.enqueue(QueueType.MIDJOURNEY, midjourneyCatchVo1);
//                }else {
//                    MidjourneyMqVo midjourneyCatchVo2 = new MidjourneyMqVo("oKV5h573LHrMldsy1VnoIHLJBMWE", messageId);
//                    redisUtils.enqueue(QueueType.MIDJOURNEY, midjourneyCatchVo2);
//                }
//            }
//
//        }
//    }
//    @Value("${queue.command.name}")
//    private String MQ_COMMAND_NAME;
//
//    @Autowired
//    private RabbitTemplate rabbitTemplate;
//    @Test
//    public void testMq() throws IOException {
//        String authorization = "MTA4MTgwOTAzNzY1NTU1NjA5Ng.G052C-.KLdcVi-WToPmruyC19z0R6UDz4JDNGakVqXZ7g";
//        String applicationId = "936929561302675456";
//        String guildId = "1093749340285173810";
//        String channelId = "1093749643051020378";
//        long messageId = (long) (Math.random() * 999999999L);
//        DiscordHttpInteractionVo command = MidjourneyUtils.getCommand(applicationId, guildId, channelId, "cat", messageId);
//        boolean isSend = midjourneyUtils.sendCommand(authorization,command);
//        if (isSend){
//            rabbitTemplate.convertAndSend(MQ_COMMAND_NAME, new MidjourneyMqVo("oKV5h5x1mFdgv3cuUmzMzXn56o8Y", messageId));
//        }else {
//            System.out.println("发送失败");
//        }
//    }
//    @Autowired
//    private FileUtils fileUtils;
//    @Test
//    public void testUpload(){
//        String s = fileUtils.uploadAndGetUrl(new File("C:\\Users\\Ms Tong\\Desktop\\c075780e2b51e68a3f1b8e85712cef33_1.jpg"));
//        System.out.println(s);
//    }
}
