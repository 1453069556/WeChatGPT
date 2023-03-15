package com.gpt.chatproject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpt.chatproject.utils.GptUtils;
import com.gpt.chatproject.vo.WxRedisCatchVo;
import com.theokanning.openai.OpenAiApi;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.kefu.WxMpKefuMessage;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import retrofit2.Retrofit;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.theokanning.openai.service.OpenAiService.*;


@SpringBootTest
class ChatProjectApplicationTests {
//
//    final static String TOKEN = "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";
//    final static String HOST_NAME = "127.0.0.1";
//
//    ChatMessage contextLoads() {
//        // 设置代理
//        ObjectMapper mapper = defaultObjectMapper();
//        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(HOST_NAME, 10809));
//        OkHttpClient client = defaultClient(TOKEN, Duration.ofSeconds(30L)).newBuilder().proxy(proxy).build();
//        Retrofit retrofit = defaultRetrofit(client, mapper);
//        OpenAiApi api = retrofit.create(OpenAiApi.class);
//        // 创建 OpenAI 客户端
//        OpenAiService service = new OpenAiService(api);
//        // 创建测试消息
//        ChatMessage testMessage = new ChatMessage("user", "说这是一个测试。");
//        // 创建消息列表
//        List<ChatMessage> messages = new ArrayList<>();
//        messages.add(testMessage);
//        // 设置请求参数
//        ChatCompletionRequest request = ChatCompletionRequest.builder().messages(messages).model("gpt-3.5-turbo").build();
//        // 调用 GPT-3 API
//        ChatCompletionResult chatCompletion = service.createChatCompletion(request);
//
//        // 打印 API 返回结果
//        return chatCompletion.getChoices().get(0).getMessage();
//    }
//
//    @Autowired
//    GptUtils gptUtils;
//
//    @Test
//    void test2() {
//        ArrayList<ChatMessage> messages = new ArrayList<>();
//        messages.add(new ChatMessage("user", "说这是一个测试。"));
//        ChatMessage chatMessage = gptUtils.askGpt(messages);
//        System.out.println(chatMessage.getContent());
//    }
//
//    @Autowired
//    WxMpService wxMpService;
//
//    @Test
//    void test1() throws WxErrorException {
//        WxMpKefuMessage kefuMessage = WxMpKefuMessage.TEXT()
//                .toUser("o0tN_509wVyIjaQSLcKaBlH_e8FE")
//                .content("测试主动推送")
//                .build();
//        wxMpService.getKefuService().sendKefuMessage(kefuMessage);
//
//    }
//    @Test
//    void test3(){
//    }

}
