package com.gpt.chatproject.config;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.gpt.chatproject.interceptor.RequestLoggingInterceptor;
import com.gpt.chatproject.vo.RedisLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestLoggingInterceptor());
    }

    @Bean
    public XmlMapper xmlMapper() {
        return new XmlMapper();
    }

}
