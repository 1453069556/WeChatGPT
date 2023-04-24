package com.gpt.chatproject.listener;

import com.gpt.chatproject.dao.MemberInfoDao;
import com.gpt.chatproject.utils.WeChatUtils;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RedisKeyExpirationListener implements MessageListener {
    @Autowired
    private MemberInfoDao memberInfoDao;
    @Autowired
    private WeChatUtils weChatUtils;
    private static final String CHAT_PREFIX = "chat_catch:";

    @Override
    public void onMessage(Message message, byte[] pattern) {
        // 获取过期的key
        String expiredKey = message.toString();
        if (expiredKey.startsWith(CHAT_PREFIX)) {
            String openId = expiredKey.split(CHAT_PREFIX)[1];
            try {
                weChatUtils.sendKefuTextMessage(openId, "小C告退~");
            } catch (WxErrorException e) {
                log.error(e.getMessage());
            }
        }
    }
}
