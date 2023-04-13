package com.gpt.chatproject.constant;

public class MidjourneyConstant {
    private static String MESSAGES;

    public static synchronized String getMessages() {
        return MESSAGES;
    }

    public static synchronized void setMessages(String messages) {
        MESSAGES = messages;
    }
}
