package com.gpt.chatproject.vo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class RedisLock {
    @Autowired
    private RedisTemplate redisTemplate;
    private static final String LOCK_PREFIX = "lock:";
    // 锁的过期时间，单位为秒
    @Value("${redislock.expireSeconds}")
    private int EXPIRE_SECONDS;

    /**
     * 尝试获取锁
     *
     * @param key 锁的 key
     * @return 如果获取锁成功，返回 true；否则，返回 false
     */
    public boolean tryLock(String key) {
        String lockKey = LOCK_PREFIX + key;
        // 尝试获取锁
        boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, UUID.randomUUID().toString());
        if (success) {
            // 获取锁成功，设置锁的过期时间
            redisTemplate.expire(lockKey, EXPIRE_SECONDS, TimeUnit.SECONDS);
            return true;
        }
        return false;
    }

    /**
     * 释放锁
     *
     * @param key 锁的 key
     */
    public void releaseLock(String key) {
        String lockKey = LOCK_PREFIX + key;
        redisTemplate.delete(lockKey);
    }

}
