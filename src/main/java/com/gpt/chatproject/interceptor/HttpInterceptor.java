package com.gpt.chatproject.interceptor;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@Slf4j
public class HttpInterceptor implements Interceptor {
    @Override
    public @NotNull Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String url = request.url().toString(); // 获取请求的网址
        log.debug("Request URL: " + url);
        Response response = chain.proceed(request);
        ResponseBody responseBody = response.peekBody(Long.MAX_VALUE); // 获取响应体
        log.debug("Response Body: " + responseBody.string());
        return response;
    }
}
