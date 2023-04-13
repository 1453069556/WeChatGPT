package com.gpt.chatproject.utils;

import com.gpt.chatproject.enums.HttpEnum;
import com.gpt.chatproject.vo.DiscordInteractionVo;
import com.gpt.chatproject.vo.DiscordMessageVo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MidjourneyUtils {
    // 获取消息列表
    // https://discord.com/api/v9/channels/{channel_id}/messages?limit=100&before=1234567890&after=1234567800

    /**
     * 发送指令给 discord
     *
     * @param authorization        session
     * @param discordInteractionVo json体
     */
    public static void sendCommand(String authorization, DiscordInteractionVo discordInteractionVo) throws IOException {
        String sendUrl = "https://discord.com/api/v9/interactions";
        String promptJson = JsonUtils.toJson(discordInteractionVo);
        HashMap<HttpEnum, String> header = new HashMap<>();
        header.put(HttpEnum.HEADER_NAME, "authorization");
        header.put(HttpEnum.HEADER_VALUE, authorization);
        HttpUtils.postJson(header, sendUrl, promptJson);
    }

    /**
     * 获取所有的消息内容并设置limit
     *
     * @param authorization authorization
     * @param channelId     channelId
     * @return String
     */
    public static String getMessages(String authorization, String channelId, Integer limit) {
        String getUrl = String.format("https://discord.com/api/v9/channels/%s/messages", channelId);
        HashMap<HttpEnum, String> header = new HashMap<>();
        header.put(HttpEnum.HEADER_NAME, "authorization");
        header.put(HttpEnum.HEADER_VALUE, authorization);
        HashMap<String, String> param = new HashMap<>();
        param.put("limit", limit.toString());
        return HttpUtils.get(header, getUrl, param);
    }


    /**
     * 根据messageId 获取消息内容
     *
     * @param messages  消息列表messages
     * @param messageId messageId
     * @return 实体
     */
    public static DiscordMessageVo getMessageByMessageId(String messages, long messageId) {
        DiscordMessageVo[] discordMessageVo = JsonUtils.fromJsonArray(messages, DiscordMessageVo.class);
        assert discordMessageVo != null;
        return matchMessages(discordMessageVo, messageId);
    }

    /**
     * 过滤一条消息
     *
     * @param discordMessageVos 消息列表
     * @param messageId         消息ID
     * @return 消息实体
     */
    public static DiscordMessageVo matchMessages(DiscordMessageVo[] discordMessageVos, long messageId) {
        for (DiscordMessageVo discordMessageVo : discordMessageVos) {
            if (discordMessageVo.getContent().contains(String.format("--seed %09d", messageId))) {
                return discordMessageVo;
            }
        }
        return null;
    }

    /**
     * Midjourney discord指令发送模板
     *
     * @param guildId   服务器ID
     * @param channelId 频道ID
     * @param prompt    咒语
     * @return json模板类
     */
    public static DiscordInteractionVo getCommand(String applicationId, String guildId, String channelId, String prompt, long messageId) {
        if (messageId < 0 || messageId > 999999999L) {
            throw new IllegalArgumentException("messageId必须是0到999999999之间的整数");
        }
        DiscordInteractionVo interaction = new DiscordInteractionVo();
        // 填充基本信息
        interaction.setType(2); // 交互类型，2表示消息组件交互
        interaction.setApplicationId(applicationId); // 应用程序ID
        interaction.setGuildId(guildId); // 服务器ID
        interaction.setChannelId(channelId); // 频道ID
        interaction.setSessionId("69db23b71be9a80ba9636cfa82c53ddf"); // 会话ID

        // 生成options模块
        DiscordInteractionVo.InteractionData interactionData = new DiscordInteractionVo.InteractionData();
        interactionData.setVersion("1077969938624553050"); // 组件版本号
        interactionData.setId("938956540159881230"); // 组件ID
        interactionData.setName("imagine"); // 组件名称
        interactionData.setType(1); // 组件类型，1表示消息组件

        DiscordInteractionVo.InteractionData.Option option = new DiscordInteractionVo.InteractionData.Option();
        option.setType(3); // 选项类型，3表示字符串
        option.setName("prompt"); // 选项名称
        option.setValue(prompt + " jpg " + String.format(" --seed %09d", messageId)); // 填充选项值, 由于需要消息定位，无奈之下这里加上自定义的唯一标识

        List<DiscordInteractionVo.InteractionData.Option> optionList = new ArrayList<>();
        optionList.add(option);
        interactionData.setOptions(optionList); // 添加选项列表到交互数据中

        // 生成application_command
        DiscordInteractionVo.InteractionData.ApplicationCommand app_command = new DiscordInteractionVo.InteractionData.ApplicationCommand();
        app_command.setId("938956540159881230"); // 应用程序组件ID
        app_command.setApplicationId(applicationId); // 应用程序ID
        app_command.setVersion("1077969938624553050"); // 组件版本号
        app_command.setType(1); // 组件类型，1表示消息组件
        app_command.setNsfw(false); // 是否为不安全内容，false表示安全
        app_command.setName("imagine"); // 组件名称
        app_command.setDescription("Create images with Midjourney"); // 组件描述
        app_command.setDmPermission(true); // 是否有直接消息权限，true表示有

        List<DiscordInteractionVo.InteractionData.ApplicationCommand.Option> applicationCommandOptionList = new ArrayList<>();
        DiscordInteractionVo.InteractionData.ApplicationCommand.Option appCommandOption = new DiscordInteractionVo.InteractionData.ApplicationCommand.Option();
        appCommandOption.setType(3); // 选项类型，3表示字符串
        appCommandOption.setName("prompt"); // 选项名称
        appCommandOption.setDescription("The prompt to imagine"); // 选项描述
        appCommandOption.setRequired(true); // 是否必填选项，true表示必填
        applicationCommandOptionList.add(appCommandOption);
        app_command.setOptions(applicationCommandOptionList); // 添加选项列表到应用程序组件中

        interactionData.setApplicationCommand(app_command);
        interaction.setData(interactionData); // 添加交互数据到实体类中

//        interaction.setNonce(); // 随机数，用于后续的验证
        return interaction;
    }
}
