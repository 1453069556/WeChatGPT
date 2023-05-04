package com.gpt.chatproject.utils;

import com.gpt.chatproject.dao.MemberInfoDao;
import com.gpt.chatproject.entity.MemberInfo;
import com.gpt.chatproject.enums.ChatType;
import com.gpt.chatproject.enums.MemberLevel;
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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Log4j2
public class RedisUtils {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private MemberInfoDao memberInfoDao;
    private static final String CHAT_PREFIX = "chat_catch:";
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
     * 获取时长锁的值
     *
     * @param Key
     * @return
     */
    public Integer getTimeLock(String Key) {
        String lockKey = TIME_LOCK_PREFIX + Key;
        Object timeLock = redisTemplate.opsForValue().get(lockKey);
        if (timeLock == null) {
            return 0;
        }
        return Integer.parseInt(timeLock.toString());
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
        String chatKey = CHAT_PREFIX + fromUser;
        try {
            WxRedisCatchVo wxRedisCatchVo = (WxRedisCatchVo) redisTemplate.opsForValue().get(chatKey);
            if (!ObjectUtils.isEmpty(wxRedisCatchVo)) {
                ArrayList<ChatMessage> chatCatch = wxRedisCatchVo.getChatCatch();
                chatCatch.add(new ChatMessage(role, content));
                wxRedisCatchVo.setChatCatch(chatCatch);
                wxRedisCatchVo.setChatCount(wxRedisCatchVo.getChatCount() + 1);
                redisTemplate.opsForValue().set(chatKey, wxRedisCatchVo, CHAT_TIME_OUT, TimeUnit.SECONDS);
                return true;
            }
            // 查询出来为空，说明首次聊天，新建记录
            WxRedisCatchVo newWxRedisCatchVo = new WxRedisCatchVo(CHAT_MAX_CATCH);
            ArrayList<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage(role, content));
            newWxRedisCatchVo.setChatCatch(messages);
            newWxRedisCatchVo.setChatCount(newWxRedisCatchVo.getChatCount() + 1);
            WxRedisCatchVo catchVo = loadMember(newWxRedisCatchVo, fromUser);
            redisTemplate.opsForValue().set(chatKey, catchVo, CHAT_TIME_OUT, TimeUnit.SECONDS);
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
     * @param fromUser fromUser
     * @param chatType chatType
     * @return boolean
     */
    public boolean updateChatCatchType(String fromUser, ChatType chatType) {
        String chatKey = CHAT_PREFIX + fromUser;
        try {
            // 状态更改，初始化聊天缓存
            WxRedisCatchVo newWxRedisCatchVo = new WxRedisCatchVo(CHAT_MAX_CATCH, chatType);
            ArrayList<ChatMessage> messages = new ArrayList<>();
            newWxRedisCatchVo.setChatCatch(messages);
            WxRedisCatchVo catchVo = loadMember(newWxRedisCatchVo, fromUser);
            redisTemplate.opsForValue().set(chatKey, catchVo, CHAT_TIME_OUT, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            log.debug(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 设置会员状态
     *
     * @return
     */
    public void setMemberLevel(String fromUser, MemberLevel memberLevel) {
        String chatKey = CHAT_PREFIX + fromUser;
        WxRedisCatchVo catchVo = (WxRedisCatchVo) redisTemplate.opsForValue().get(chatKey);
        if (catchVo == null) {
            WxRedisCatchVo wxRedisCatchVo = loadMember(new WxRedisCatchVo(CHAT_MAX_CATCH, ChatType.NORMAL), fromUser);
            redisTemplate.opsForValue().set(chatKey, wxRedisCatchVo, CHAT_TIME_OUT, TimeUnit.SECONDS);
            return;
        }
        catchVo.setMemberLevel(memberLevel.getType());
        redisTemplate.opsForValue().set(chatKey, catchVo, CHAT_TIME_OUT, TimeUnit.SECONDS);
    }

    /**
     * 获取聊天缓存
     *
     * @param fromUser
     * @return
     */
    public WxRedisCatchVo getCatch(String fromUser) {
        String chatKey = CHAT_PREFIX + fromUser;
        Object result = redisTemplate.opsForValue().get(chatKey);
        if (result == null) {
            WxRedisCatchVo wxRedisCatchVo = loadMember(new WxRedisCatchVo(CHAT_MAX_CATCH, ChatType.NORMAL), fromUser);
            redisTemplate.opsForValue().set(chatKey, wxRedisCatchVo, CHAT_TIME_OUT, TimeUnit.SECONDS);
            return wxRedisCatchVo;
        }
        return (WxRedisCatchVo) result;
    }

    /**
     * 刷新缓存过期时间
     *
     * @param fromUser
     * @return
     */
    public void resetCatchExpire(String fromUser) {
        String chatKey = CHAT_PREFIX + fromUser;
        redisTemplate.expire(chatKey, CHAT_TIME_OUT, TimeUnit.SECONDS);
    }

    /**
     * 加载会员信息
     *
     * @param wxRedisCatchVo
     * @param fromUser
     * @return
     */
    private WxRedisCatchVo loadMember(WxRedisCatchVo wxRedisCatchVo, String fromUser) {
        MemberInfo memberInfo = memberInfoDao.findByUserId(fromUser);
        if (memberInfo != null) {
            long expireTime = MyDateUtils.formatDate(memberInfo.getExpireTime(), "yyyyMMddHHmmss").getTime();
            long nowTime = MyDateUtils.getBeijingDate().getTime();
            if (expireTime - nowTime > 0) {
                String memberLevel = memberInfo.getMemberLevel();
                wxRedisCatchVo.setMemberLevel(memberLevel);
                wxRedisCatchVo.setImageNum(memberInfo.getImageNum());
            }
            // TODO 这里可以考虑加上到期之前所剩余的绘图次数
        }
        return wxRedisCatchVo;
    }


    /**
     * 减少图片使用机会
     *
     * @param catchVo  缓存
     * @param fromUser fromUser
     * @return
     */
    public boolean decrImageNum(WxRedisCatchVo catchVo, String fromUser) {
        String chatKey = CHAT_PREFIX + fromUser;
        int imageNum = catchVo.getImageNum();
        if (imageNum > 0) {
            catchVo.setImageNum(imageNum - 1);
            redisTemplate.opsForValue().set(chatKey, catchVo, CHAT_TIME_OUT, TimeUnit.SECONDS);
            return true;
        }
        return false;
    }

    /**
     * 更新图片操作次数
     *
     * @param imageNum imageNum
     * @param fromUser fromUser
     * @return
     */
    public boolean updateImageNum(String fromUser, int imageNum) {
        String chatKey = CHAT_PREFIX + fromUser;
        WxRedisCatchVo catchVo = getCatch(chatKey);
        catchVo.setImageNum(catchVo.getImageNum() + imageNum);
        redisTemplate.opsForValue().set(chatKey, catchVo, CHAT_TIME_OUT, TimeUnit.SECONDS);
        return false;
    }

    /**
     * 获取所有的缓存键
     *
     * @return Set<String>
     */
    public Set<String> getWxRedisCatchVoKeys() {
        return redisTemplate.keys(CHAT_PREFIX + "*");
    }
}
