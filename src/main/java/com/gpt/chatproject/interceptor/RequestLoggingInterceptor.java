package com.gpt.chatproject.interceptor;

import lombok.extern.java.Log;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Map;

@Log
public class RequestLoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 打印请求信息
        log.info("Request URL: " + request.getRequestURL());
        log.info("Request Method: " + request.getMethod());
        log.info("Request Parameters: " + getParams(request));
        return true;
    }

    private String getParams(HttpServletRequest request) {
        // 将请求参数转换为字符串
        Map<String, String[]> params = request.getParameterMap();
        log.info("Request Parameters: " + params.toString());
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            sb.append(entry.getKey())
                    .append("=")
                    .append(Arrays.toString(entry.getValue()))
                    .append("&");
        }
        return sb.toString();
    }
}
