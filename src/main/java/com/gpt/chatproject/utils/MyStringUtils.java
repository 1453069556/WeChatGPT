package com.gpt.chatproject.utils;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyStringUtils {
    private static final Charset charset = StandardCharsets.UTF_8;
    private static final CharsetDecoder decoder = charset.newDecoder();

    public static List<String> splitString(String input, int maxByteSize) {
        byte[] bytes = input.getBytes(charset);
        int length = bytes.length;
        int numOfChunks = (int) Math.ceil((double) length / maxByteSize);
        List<String> chunks = new ArrayList<>(numOfChunks);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        CharBuffer charBuffer = CharBuffer.allocate(maxByteSize);
        for (int i = 0; i < numOfChunks; i++) {
            byteBuffer.limit(Math.min((i + 1) * maxByteSize, length));
            byteBuffer.position(i * maxByteSize);
            CoderResult coderResult = decoder.decode(byteBuffer, charBuffer, true);
            if (coderResult.isError()) {
                throw new RuntimeException("Failed to decode input");
            }
            charBuffer.flip();
            chunks.add(charBuffer.toString());
            charBuffer.clear();
        }
        return chunks;
    }

    /**
     * 正则匹配
     * @param regex 正则表达式
     * @param input 需过滤的字符串
     * @return true代表匹配成功
     */
    public static String matchString(String regex, String input) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return null; // 或者抛出一个异常
        }
    }

    /**
     * 指定数量随机字符串
     *
     * @param length
     * @return
     */
    public static String generateRandomString(int length) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return random.ints(length, 0, 36)
                .mapToObj(i -> Integer.toString(i, 36))
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }
}
