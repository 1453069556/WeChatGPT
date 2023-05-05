package com.gpt.chatproject.constant;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 消费者数量统计
 */
public class ConsumerCounterTotal {
    private static AtomicInteger counter = new AtomicInteger(0);
    public static int incrementAndGet() {
        return counter.incrementAndGet();
    }

    public static int decrementAndGet() {
        return counter.decrementAndGet();
    }

    public static int get() {
        return counter.get();
    }
}
