package com.gpt.chatproject.interceptor;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class HttpInterceptor implements Interceptor {
    @Override
    public @NotNull Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String url = request.url().toString(); // 获取请求的网址
        System.out.println("Request URL: " + url);
        Response response = chain.proceed(request);
        ResponseBody responseBody = response.peekBody(Long.MAX_VALUE); // 获取响应体
        System.out.println("Response Body: " + responseBody.string());
        return response;
    }
}
