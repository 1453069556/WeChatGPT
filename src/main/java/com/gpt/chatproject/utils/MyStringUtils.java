package com.gpt.chatproject.utils;

import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
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
}
