package com.gpt.chatproject.Scheduled;

import com.gpt.chatproject.dao.MemberInfoDao;
import com.gpt.chatproject.entity.MemberInfo;
import com.gpt.chatproject.utils.RedisUtils;
import com.gpt.chatproject.vo.WxRedisCatchVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.regex.Pattern;

@Component
@EnableScheduling
public class RedisScheduled {
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private MemberInfoDao memberInfoDao;
    private static final String CHAT_PREFIX = "chat_catch:";

    // 定期同步剩余使用次数到数据库
    @Scheduled(fixedRate = 60000) // 每隔60秒执行一次
    public void syncUsageCountsToDatabase() {
        // 获取所有聊天缓存键
        Set<String> extraKeys = redisUtils.getWxRedisCatchVoKeys();
        for (String extraKey : extraKeys) {
            String[] keyParts = extraKey.split(Pattern.quote(CHAT_PREFIX));
            if (keyParts.length >= 2) {
                String userId = keyParts[1];
                // 在这里执行同步用户使用次数到数据库的逻辑
                WxRedisCatchVo catchVo = redisUtils.getCatch(userId);
                if (catchVo.getMemberLevel() != null) {
                    int imageNum = catchVo.getImageNum();
                    syncUsageCountsToDatabase(userId, imageNum);
                }
            }
        }
    }

    /**
     * 更新数据库会员信息的绘图次数
     * @param fromUser fromUser
     * @param imageNum imageNum
     */
    @Transactional
    void syncUsageCountsToDatabase(String fromUser, int imageNum) {
        MemberInfo memberInfo = memberInfoDao.findByUserId(fromUser);
        memberInfo.setImageNum(imageNum);
        memberInfoDao.update(memberInfo);
    }
}
