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
    private static final String CHAT_LOCK_PREFIX = "chat_lock:";
    private static final String TIME_LOCK_PREFIX = "time_lock:";
    // 会话锁的过期时间，单位为秒
    @Value("${redislock.chatExpireSeconds}")
    private int CHAT_EXPIRE_SECONDS;

    // 时长频率锁的过期时间，单位为秒
    @Value("${redislock.timeExpireSeconds}")
    private int TIME_EXPIRE_SECONDS;
    // 时长频率锁会话的最大频率（次）
    @Value("${redislock.timeMaxCount}")
    private int TIME_MAX_COUNT;

    @Value("${wxchat.chatMaxCatch}")
    private Integer CHAT_MAX_CATCH;

    @Value("${redislock.chatTimeOut}")
    private Integer CHAT_TIME_OUT;


    public long getExpireByKey(String key){
        String lockKey = TIME_LOCK_PREFIX + key;
        return redisTemplate.getExpire(lockKey);
    }

    /**
     * 尝试获取时长频率锁
     *
     * @param key
     * @return
     */
    public boolean tryTimeLock(String key) {
        String lockKey = TIME_LOCK_PREFIX + key;
        Object timeLock = redisTemplate.opsForValue().get(lockKey);
        if (ObjectUtils.isEmpty(timeLock)) {
            // 首次设定为1
            redisTemplate.opsForValue().increment(lockKey, 1);
            // 首次设定超时时间
            redisTemplate.expire(lockKey, TIME_EXPIRE_SECONDS, TimeUnit.SECONDS);
        } else {
            if (Integer.parseInt(timeLock.toString()) > TIME_MAX_COUNT) {
                return false;
            }
            // 增长1
            redisTemplate.opsForValue().increment(lockKey, 1);
        }
        return true;
    }


    /**
     * 释放时长频率锁
     *
     * @param key 锁的 key
     */
    public void releaseTimeLock(String key) {
        // TODO 二维码关注解锁
        String lockKey = TIME_LOCK_PREFIX + key;
        redisTemplate.delete(lockKey);
    }

    /**
     * 尝试获取会话锁
     *
     * @param key 锁的 key
     * @return 如果获取锁成功，返回 true；否则，返回 false
     */
    public boolean tryChatLock(String key) {
        String lockKey = CHAT_LOCK_PREFIX + key;
        // 尝试获取锁
        boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, UUID.randomUUID().toString());
        if (success) {
            // 获取锁成功，设置锁的过期时间
            redisTemplate.expire(lockKey, CHAT_EXPIRE_SECONDS, TimeUnit.SECONDS);
            return true;
        }
        return false;
    }

    /**
     * 释放会话锁
     *
     * @param key 锁的 key
     */
    public void releaseChatLock(String key) {
        String lockKey = CHAT_LOCK_PREFIX + key;
        redisTemplate.delete(lockKey);
    }

    /**
     * 上下文缓存
     *
     * @param fromUser
     * @param role
     * @param content
     * @return
     */
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
