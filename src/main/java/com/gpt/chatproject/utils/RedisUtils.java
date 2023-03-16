package com.gpt.chatproject.utils;

import com.gpt.chatproject.vo.WxRedisCatchVo;
import com.theokanning.openai.completion.chat.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtils {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    private static final String LOCK_PREFIX = "lock:";
    // 锁的过期时间，单位为秒
    @Value("${redislock.expireSeconds}")
    private int EXPIRE_SECONDS;

    @Value("${wxchat.chatMaxCatch}")
    private Integer CHAT_MAX_CATCH;

    @Value("${redislock.chatTimeOut}")
    private Integer CHAT_TIME_OUT;

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

    // redis缓存处理
    public boolean catchChat(String fromUser, String role, String content) {
        try {
            WxRedisCatchVo wxRedisCatchVo = (WxRedisCatchVo) redisTemplate.opsForValue().get(fromUser);
            if (!ObjectUtils.isEmpty(wxRedisCatchVo)) {
                ArrayList<ChatMessage> chatCatch = wxRedisCatchVo.getChatCatch();
                chatCatch.add(new ChatMessage(role, content));
                wxRedisCatchVo.setChatCatch(chatCatch);
                wxRedisCatchVo.setChatCount(wxRedisCatchVo.getChatCount() + 1);
                redisTemplate.opsForValue().set(fromUser, wxRedisCatchVo, CHAT_TIME_OUT, TimeUnit.SECONDS);
                return true;
            }
            // 查询出来为空，说明首次聊天，新建记录
            WxRedisCatchVo newWxRedisCatchVo = new WxRedisCatchVo(CHAT_MAX_CATCH);
            ArrayList<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage(role, content));
            newWxRedisCatchVo.setChatCatch(messages);
            newWxRedisCatchVo.setChatCount(newWxRedisCatchVo.getChatCount() + 1);
            redisTemplate.opsForValue().set(fromUser, newWxRedisCatchVo, CHAT_TIME_OUT, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public WxRedisCatchVo getCatch(String fromUser) {
        return (WxRedisCatchVo) redisTemplate.opsForValue().get(fromUser);
    }
}
