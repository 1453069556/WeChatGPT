package com.gpt.chatproject.utils;

import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@Service
public class MyStringUtils {
    public List<String> splitString(String input, int maxByteSize) throws UnsupportedEncodingException {
        byte[] bytes = input.getBytes("UTF-8");
        int length = bytes.length;
        int numOfChunks = (int) Math.ceil((double) length / maxByteSize);
        List<String> chunks = new ArrayList<>(numOfChunks);
        for (int i = 0; i < numOfChunks; i++) {
            int start = i * maxByteSize;
            int end = Math.min((i + 1) * maxByteSize, length);
            chunks.add(new String(bytes, start, end - start, "UTF-8"));
        }
        return chunks;
    }


}
