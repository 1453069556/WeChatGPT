package com.gpt.chatproject.utils;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpt.chatproject.enums.ChatType;
import com.gpt.chatproject.enums.GptRoleType;
import com.theokanning.openai.OpenAiApi;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.extern.log4j.Log4j2;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import retrofit2.Retrofit;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.util.List;

import static com.gpt.chatproject.enums.GptModelType.GPT_TURBO;
import static com.theokanning.openai.service.OpenAiService.*;


@Component
@Log4j2
public class GptUtils {
    @Value("${openai.token}")
    private String TOKEN;
    @Value("${openai.timeout}")
    private long TIME_OUT;
    @Value("${openai.temperature}")
    private double TEMPERATURE;
    @Value("${openai.presence_penalty}")
    private double PRESENCE_PENALTY;
    @Value("${openai.frequency_penalty}")
    private double FREQUENCY_PENALTY;
    @Value("${openai.n}")
    private Integer N;
    @Value("${openai.max_tokens}")
    private Integer max_tokens;
    @Value("${openai.stream}")
    private boolean stream;
    @Value("${openai.system_default}")
    private String SYSTEM_DEFAULT;
    @Value("${openai.image_chat_default}")
    private String IMAGE_CHAT_DALL_DEFAULT;
    @Value("${openai.use_proxy}")
    private Integer USE_PROXY;
    final static String PROXY_HOST_NAME = "127.0.0.1";
    final static Integer PROXY_PORT = 10810;


    public OpenAiApi initApi(long timeout) {
        // 设置代理
        ObjectMapper mapper = defaultObjectMapper();
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(PROXY_HOST_NAME, PROXY_PORT));
        OkHttpClient client;
        if (USE_PROXY == 0) {
            client = defaultClient(TOKEN, Duration.ofSeconds(timeout)).newBuilder().build();
        } else {
            client = defaultClient(TOKEN, Duration.ofSeconds(timeout)).newBuilder().proxy(proxy).build();
        }
        Retrofit retrofit = defaultRetrofit(client, mapper);
        return retrofit.create(OpenAiApi.class);
    }

    /**
     * 与GPT-3.5 turb对话
     *
     * @param messages 会话
     * @return ChatMessage
     * @throws Exception
     */
    public ChatMessage askGpt(List<ChatMessage> messages, ChatType chatType) throws Exception {
        // 创建 OpenAI 客户端
        OpenAiService service = new OpenAiService(initApi(TIME_OUT));
        switch (chatType) {
            case NORMAL:
                messages.add(0, new ChatMessage(GptRoleType.SYSTEM.getRole(), SYSTEM_DEFAULT));
                break;
            case IMAGE_DALL:
            case IMAGE_MIDJOURNEY:
                messages.add(0, new ChatMessage(GptRoleType.SYSTEM.getRole(), IMAGE_CHAT_DALL_DEFAULT));
                break;
        }
        // 设置请求参数
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .messages(messages)
                .model(GPT_TURBO.getType())
                .temperature(TEMPERATURE)
                .presencePenalty(PRESENCE_PENALTY)
                .frequencyPenalty(FREQUENCY_PENALTY)
                .n(N)
                .maxTokens(max_tokens)
                .stream(stream)
                .build();
        // 调用 GPT-3 API
        ChatCompletionResult chatCompletion = service.createChatCompletion(request);
        ChatMessage gptResult = chatCompletion.getChoices().get(0).getMessage();
        gptResult.setContent(gptResult.getContent().replaceFirst("(\\n)+", ""));
        // 打印 API 返回结果
        return chatCompletion.getChoices().get(0).getMessage();
    }
}
