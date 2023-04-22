package com.gpt.chatproject.utils;

import com.gpt.chatproject.dao.MemberInfoDao;
import com.gpt.chatproject.entity.MemberInfo;
import com.gpt.chatproject.enums.ChatType;
import com.gpt.chatproject.vo.MidjourneyRedisVo;
import com.gpt.chatproject.vo.WxRedisCatchVo;
import com.theokanning.openai.completion.chat.ChatMessage;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Log4j2
public class RedisUtils {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private MemberInfoDao memberInfoDao;
    private static final String CHAT_LOCK_PREFIX = "chat_lock:";
    private static final String TIME_LOCK_PREFIX = "time_lock:";
    private static final String PIC_LOCK_PREFIX = "pic_lock:";
    // 会话锁的过期时间，单位为秒
    @Value("${redislock.chatExpireSeconds}")
    private int CHAT_EXPIRE_SECONDS;

    @Value("${redislock.aiPicExpireSeconds}")
    private int PIC_EXPIRE_SECONDS;

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

    /**
     * 5分钟的缓存服务供MidjourneyRedisCatch
     *
     * @param midjourneyRedisVo midjourneyRedisVo
     */
    public void midjourneyRedisCatch(MidjourneyRedisVo midjourneyRedisVo) {
        String Prefix = "MidjourneyRedisCatch:";
        redisTemplate.opsForValue().set(Prefix + midjourneyRedisVo.getUserId(), midjourneyRedisVo, 5, TimeUnit.MINUTES);
    }

    public MidjourneyRedisVo getMidjourneyRedisCatch(String fromUser) {
        String Prefix = "MidjourneyRedisCatch:";
        return (MidjourneyRedisVo) redisTemplate.opsForValue().get(Prefix + fromUser);
    }

    /**
     * 获取剩余时间
     *
     * @param key key
     * @return
     */
    public long getExpireByKey(String key) {
        String lockKey = TIME_LOCK_PREFIX + key;
        return redisTemplate.getExpire(lockKey);
    }

    /**
     * 尝试获取时长频率锁
     *
     * @param key key
     * @return
     */
    public boolean tryTimeLock(String key, long delta) {
        String lockKey = TIME_LOCK_PREFIX + key;
        Object timeLock = redisTemplate.opsForValue().get(lockKey);
        if (ObjectUtils.isEmpty(timeLock)) {
            // 首次设定为1
            redisTemplate.opsForValue().increment(lockKey, delta);
            // 首次设定超时时间
            redisTemplate.expire(lockKey, TIME_EXPIRE_SECONDS, TimeUnit.SECONDS);
        } else {
            if (Integer.parseInt(timeLock.toString()) > TIME_MAX_COUNT) {
                return false;
            }
            // 增长1
            redisTemplate.opsForValue().increment(lockKey, delta);
        }
        return true;
    }

    /**
     * 时长频率锁回退1
     *
     * @param key
     * @return
     */
    public void timeLockFallback(String key) {
        String lockKey = TIME_LOCK_PREFIX + key;
        if (!ObjectUtils.isEmpty(redisTemplate.opsForValue().get(lockKey))) {
            // 首次设定为1
            redisTemplate.opsForValue().increment(lockKey, -1);
        }
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
     * 尝试获取AI绘图锁
     *
     * @param key key
     * @return
     */
    public boolean tryAiPicLock(String key) {
        String lockKey = PIC_LOCK_PREFIX + key;
        // 尝试获取锁
        boolean success = Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(lockKey, UUID.randomUUID().toString()));
        if (success) {
            // 获取锁成功，设置锁的过期时间
            redisTemplate.expire(lockKey, PIC_EXPIRE_SECONDS, TimeUnit.SECONDS);
            return true;
        }
        return false;
    }

    /**
     * 是否被锁
     *
     * @param key
     * @return
     */
    public boolean aiPicIsLock(String key) {
        String lockKey = PIC_LOCK_PREFIX + key;
        String lock = (String) redisTemplate.opsForValue().get(lockKey);
        return StringUtils.isNotBlank(lock);
    }

    /**
     * 释放绘图锁
     *
     * @param key key
     * @return
     */
    public void releasePicLock(String key) {
        String lockKey = PIC_LOCK_PREFIX + key;
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
        boolean success = Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(lockKey, UUID.randomUUID().toString()));
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
     * 默认聊天状态为【默认】
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
            WxRedisCatchVo catchVo = loadMember(newWxRedisCatchVo, fromUser);
            redisTemplate.opsForValue().set(fromUser, catchVo, CHAT_TIME_OUT, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            log.debug(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 更改当前缓存聊天状态
     *
     * @param fromUser
     * @param chatType
     * @return
     */
    public boolean updateChatCatchType(String fromUser, ChatType chatType) {
        try {
            WxRedisCatchVo wxRedisCatchVo = (WxRedisCatchVo) redisTemplate.opsForValue().get(fromUser);
            if (!ObjectUtils.isEmpty(wxRedisCatchVo)) {
                wxRedisCatchVo.setChatType(chatType);
                redisTemplate.opsForValue().set(fromUser, wxRedisCatchVo, CHAT_TIME_OUT, TimeUnit.SECONDS);
                return true;
            }
            // 查询出来为空，说明首次聊天，新建记录
            WxRedisCatchVo newWxRedisCatchVo = new WxRedisCatchVo(CHAT_MAX_CATCH, chatType);
            ArrayList<ChatMessage> messages = new ArrayList<>();
            newWxRedisCatchVo.setChatCatch(messages);
            WxRedisCatchVo catchVo = loadMember(newWxRedisCatchVo, fromUser);
            redisTemplate.opsForValue().set(fromUser, catchVo, CHAT_TIME_OUT, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            log.debug(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取聊天缓存
     *
     * @param fromUser
     * @return
     */
    public WxRedisCatchVo getCatch(String fromUser) {
        Object result = redisTemplate.opsForValue().get(fromUser);
        if (result == null) {
            WxRedisCatchVo wxRedisCatchVo = loadMember(new WxRedisCatchVo(CHAT_MAX_CATCH, ChatType.NORMAL), fromUser);
            redisTemplate.opsForValue().set(fromUser, wxRedisCatchVo, CHAT_TIME_OUT, TimeUnit.SECONDS);
            return wxRedisCatchVo;
        }
        return (WxRedisCatchVo) result;
    }

    private WxRedisCatchVo loadMember(WxRedisCatchVo wxRedisCatchVo, String fromUser) {
        MemberInfo byUserId = memberInfoDao.findByUserId(fromUser);
        if (byUserId != null) {
            String memberLevel = byUserId.getMemberLevel();
            wxRedisCatchVo.setMemberLevel(memberLevel);
        }
        return wxRedisCatchVo;
    }

    /**
     * 刷新缓存过期时间
     *
     * @param fromUser
     * @return
     */
    public void resetCatchExpire(String fromUser) {
        redisTemplate.expire(fromUser, CHAT_TIME_OUT, TimeUnit.SECONDS);
    }
}
