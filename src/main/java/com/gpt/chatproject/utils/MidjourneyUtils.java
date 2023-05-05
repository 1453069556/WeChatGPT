package com.gpt.chatproject.utils;

import com.gpt.chatproject.enums.HttpEnum;
import com.gpt.chatproject.vo.DiscordHttpCustomVo;
import com.gpt.chatproject.vo.DiscordHttpInteractionVo;
import com.gpt.chatproject.vo.DiscordHttpMessageVo;
import com.gpt.chatproject.vo.MidjourneyRedisVo;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MidjourneyUtils {
    /**
     * 发送指令给 discord
     *
     * @param authorization            session
     * @param discordHttpInteractionVo json体
     */
    public static void sendCommand(String authorization, DiscordHttpInteractionVo discordHttpInteractionVo) {
        String sendUrl = "https://discord.com/api/v9/interactions";
        String promptJson = JsonUtils.toJson(discordHttpInteractionVo);
        HashMap<HttpEnum, String> header = new HashMap<>();
        header.put(HttpEnum.HEADER_NAME, "authorization");
        header.put(HttpEnum.HEADER_VALUE, authorization);
        HttpUtils.postJson(header, sendUrl, promptJson);
    }

    /**
     * 发送Custom 指令给 discord
     *
     * @param authorization       session
     * @param discordHttpCustomVo json体
     */
    public static void sendCustomCommand(String authorization, DiscordHttpCustomVo discordHttpCustomVo) {
        String sendUrl = "https://discord.com/api/v9/interactions";
        String promptJson = JsonUtils.toJson(discordHttpCustomVo);
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
     * @param messages   messages
     * @param messageId  messageId
     * @param midRedisVo attachmentsIds
     * @return 实体
     */
    public static DiscordHttpMessageVo getMessageByMessageId(String messages, long messageId, MidjourneyRedisVo midRedisVo) {
        if (StringUtils.isBlank(messages)) {
            return null;
        }
        DiscordHttpMessageVo[] discordHttpMessageVo = JsonUtils.fromJsonArray(messages, DiscordHttpMessageVo.class);
        assert discordHttpMessageVo != null;
        return matchMessages(discordHttpMessageVo, messageId, midRedisVo);
    }

    /**
     * 过滤一条消息
     *
     * @param messageVos messageVos
     * @param messageId  messageId
     * @param midRedisVo attachmentsIds
     * @return DiscordHttpMessageVo
     */
    public static DiscordHttpMessageVo matchMessages(DiscordHttpMessageVo[] messageVos, long messageId, MidjourneyRedisVo midRedisVo) {
        for (DiscordHttpMessageVo messageVo : messageVos) {
            if (messageVo.getContent().contains(String.format("--seed %09d", messageId))) {
                List<DiscordHttpMessageVo.ReferencedMessageDTO.AttachmentsDTO> attachments = messageVo.getAttachments();
                // 如果attachmentsIds为空则代表无需过滤attachmentsId
                if (midRedisVo == null || midRedisVo.getAttachmentsIds() == null) {
                    return messageVo;
                }
                // 这个是处于提示回馈
                if (messageVo.getEmbeds().size() > 0){
                    return messageVo;
                }
                // 匹配上了
                if (attachments.size() > 0 && !midRedisVo.getAttachmentsIds().contains(attachments.get(0).getId())) {
                    return messageVo;
                }
            }
        }
        return null;
    }


    /**
     * Midjourney custom指令发送模板
     *
     * @param applicationId applicationId
     * @param guildId       guildId
     * @param channelId     channelId
     * @param customId      customId
     * @return
     */
    public static DiscordHttpCustomVo getCustomVo(String applicationId, String guildId, String channelId,
                                                  String discordMessageId, String customId) {
        DiscordHttpCustomVo customVo = new DiscordHttpCustomVo();
        customVo.setApplicationId(applicationId);
        customVo.setGuildId(guildId);
        customVo.setChannelId(channelId);
        customVo.setSessionId("ab444fba21c8a468e80fe08ef5d582a5"); // 会话ID
        customVo.setType(3);
        customVo.setMessageFlags(0);
        customVo.setMessageId(discordMessageId);

        DiscordHttpCustomVo.InteractionData interactionData = new DiscordHttpCustomVo.InteractionData();
        interactionData.setCustomId(customId);
        interactionData.setComponentType(2);
        customVo.setData(interactionData);
        return customVo;
    }

    /**
     * Midjourney discord指令发送模板
     *
     * @param guildId   服务器ID
     * @param channelId 频道ID
     * @param prompt    咒语
     * @return json模板类
     */
    public static DiscordHttpInteractionVo getCommandVo(String applicationId, String guildId, String channelId, String prompt, long messageId) {
        if (messageId < 0 || messageId > 999999999L) {
            throw new IllegalArgumentException("messageId必须是0到999999999之间的整数");
        }
        DiscordHttpInteractionVo interaction = new DiscordHttpInteractionVo();
        // 填充基本信息
        interaction.setType(2); // 交互类型，2表示消息组件交互
        interaction.setApplicationId(applicationId); // 应用程序ID
        interaction.setGuildId(guildId); // 服务器ID
        interaction.setChannelId(channelId); // 频道ID
        interaction.setSessionId("ab444fba21c8a468e80fe08ef5d582a5"); // 会话ID

        // 生成options模块
        DiscordHttpInteractionVo.InteractionData interactionData = new DiscordHttpInteractionVo.InteractionData();
        interactionData.setVersion("1077969938624553050"); // 组件版本号
        interactionData.setId("938956540159881230"); // 组件ID
        interactionData.setName("imagine"); // 组件名称
        interactionData.setType(1); // 组件类型，1表示消息组件

        DiscordHttpInteractionVo.InteractionData.Option option = new DiscordHttpInteractionVo.InteractionData.Option();
        option.setType(3); // 选项类型，3表示字符串
        option.setName("prompt"); // 选项名称
        option.setValue(" jpg " + prompt + String.format(" --seed %09d", messageId)); // 填充选项值, 由于需要消息定位，无奈之下这里加上自定义的唯一标识

        List<DiscordHttpInteractionVo.InteractionData.Option> optionList = new ArrayList<>();
        optionList.add(option);
        interactionData.setOptions(optionList); // 添加选项列表到交互数据中

        // 生成application_command
        DiscordHttpInteractionVo.InteractionData.ApplicationCommand app_command = new DiscordHttpInteractionVo.InteractionData.ApplicationCommand();
        app_command.setId("938956540159881230"); // 应用程序组件ID
        app_command.setApplicationId(applicationId); // 应用程序ID
        app_command.setVersion("1077969938624553050"); // 组件版本号
        app_command.setType(1); // 组件类型，1表示消息组件
        app_command.setNsfw(false); // 是否为不安全内容，false表示安全
        app_command.setName("imagine"); // 组件名称
        app_command.setDescription("Create images with Midjourney"); // 组件描述
        app_command.setDmPermission(true); // 是否有直接消息权限，true表示有

        List<DiscordHttpInteractionVo.InteractionData.ApplicationCommand.Option> applicationCommandOptionList = new ArrayList<>();
        DiscordHttpInteractionVo.InteractionData.ApplicationCommand.Option appCommandOption = new DiscordHttpInteractionVo.InteractionData.ApplicationCommand.Option();
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
