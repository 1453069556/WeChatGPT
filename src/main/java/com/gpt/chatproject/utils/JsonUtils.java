package com.gpt.chatproject.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 类转JSON
     *
     * @param object 类
     * @return json字符串
     */
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            // 在转换出错时，可以根据实际情况进行处理
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 通过json转成类
     *
     * @param json  json字符串
     * @param clazz 类型
     * @param <T>   返回类型
     * @return 类对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            // 在转换出错时，可以根据实际情况进行处理
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 数组json类型的转换
     * @param jsonArray
     * @param clazz
     * @return
     * @param <T>
     */
    public static <T> T[] fromJsonArray(String jsonArray, Class<T> clazz) {
        try {
            JavaType type = objectMapper.getTypeFactory().constructArrayType(clazz);
            return objectMapper.readValue(jsonArray, type);
        } catch (JsonProcessingException e) {
            // 在转换出错时，可以根据实际情况进行处理
            e.printStackTrace();
            return null;
        }
    }


    /**
     * byte类型的转换
     * @param byteArray byteArray
     * @param clazz clazz
     * @return
     * @param <T>
     */
    public static <T> T[] fromByteArray(byte[] byteArray, Class<T> clazz) {
        try {
            JavaType type = objectMapper.getTypeFactory().constructArrayType(clazz);
            return objectMapper.readValue(byteArray, type);
        } catch (IOException e) {
            // 在转换出错时，可以根据实际情况进行处理
            e.printStackTrace();
            return null;
        }
    }
}
