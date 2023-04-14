package com.gpt.chatproject.utils;

import com.gpt.chatproject.enums.HttpEnum;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

public class HttpUtils {
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    /**
     * 发送GET请求
     *
     * @param url    请求的URL
     * @param params 请求参数
     * @return 响应结果
     * @throws IOException 网络请求异常
     */
    public static String get(Map<HttpEnum, String> header, String url, Map<String, String> params) {
        String fullUrl = buildUrl(url, params);
        Request request = new Request.Builder()
                .url(fullUrl)
                .header(header.get(HttpEnum.HEADER_NAME), header.get(HttpEnum.HEADER_VALUE))
                .build();
        return getString(request);
    }

    /**
     * 发送POST请求，以JSON形式提交请求体
     *
     * @param url  请求的URL
     * @param json 请求体JSON字符串
     * @return 响应结果
     * @throws IOException 网络请求异常
     */
    public static String postJson(Map<HttpEnum, String> header, String url, String json) {
        RequestBody requestBody = RequestBody.create(JSON_MEDIA_TYPE, json);
        Request request = new Request.Builder()
                .url(url)
                .header(header.get(HttpEnum.HEADER_NAME), header.get(HttpEnum.HEADER_VALUE))
                .post(requestBody)
                .build();
        return getString(request);
    }

    @NotNull
    private static String getString(Request request) {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 10810));
        OkHttpClient client;
            client = new OkHttpClient.Builder()
//                    .proxy(proxy)
                    .readTimeout(20, TimeUnit.SECONDS).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            assert response.body() != null;
            return response.body().string();
        } catch (IOException e) {
            throw new CompletionException(e);
        }
    }

    /**
     * 构建完整的URL，将参数拼接在URL后面
     *
     * @param url    请求的URL
     * @param params 请求参数
     * @return 完整的URL
     */
    private static String buildUrl(String url, Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return url;
        }
        StringBuilder sb = new StringBuilder(url);
        if (!url.contains("?")) {
            sb.append("?");
        } else {
            sb.append("&");
        }
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            sb.append(name).append("=").append(value).append("&");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
}
