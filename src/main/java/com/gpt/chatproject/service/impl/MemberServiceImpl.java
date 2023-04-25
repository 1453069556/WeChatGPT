package com.gpt.chatproject.service.impl;

import com.gpt.chatproject.dao.MemberInfoDao;
import com.gpt.chatproject.entity.MemberInfo;
import com.gpt.chatproject.service.MemberService;
import com.gpt.chatproject.utils.MyDateUtils;
import com.gpt.chatproject.utils.RedisUtils;
import com.gpt.chatproject.vo.WxRedisCatchVo;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class MemberServiceImpl implements MemberService {
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private MemberInfoDao memberInfoDao;
    @Value("${redislock.timeMaxCount}")
    private int TIME_MAX_COUNT;

    @Override
    public String getMemberInfo(WxMpXmlMessage wxMpXmlMessage) {
        String fromUser = wxMpXmlMessage.getFromUser();
        WxRedisCatchVo catchVo = redisUtils.getCatch(wxMpXmlMessage.getFromUser());
        // 非会员的处理方案
        if (catchVo.getMemberLevel() == null) {
            StringBuilder userInfo = new StringBuilder();
            userInfo.append("您还不是会员，但您每天有基础的日常使用次数。").append("\n\n");
            Integer timeLock = redisUtils.getTimeLock(fromUser);
            userInfo.append("当前次数剩余: ").append("\n").append(TIME_MAX_COUNT - timeLock).append("次");
            return userInfo.toString();
        }
        // 以下是会员的处理方案
        int imageNum = catchVo.getImageNum();
        MemberInfo memberInfoDaoByUserId = memberInfoDao.findByUserId(fromUser);
        StringBuilder memberInfo = new StringBuilder();
        Date dateStart = MyDateUtils.formatDate(memberInfoDaoByUserId.getStartTime(), "yyyyMMddHHmmss");
        Date dateExpire = MyDateUtils.formatDate(memberInfoDaoByUserId.getExpireTime(), "yyyyMMddHHmmss");
        String dateExpireFormat = MyDateUtils.dateFormat(dateExpire, "yyyy-MM-dd HH:mm:ss");
        String dateStartFormat = MyDateUtils.dateFormat(dateStart, "yyyy-MM-dd HH:mm:ss");
        memberInfo.append("会员开通日期:").append("\n").append(dateStartFormat).append("\n\n");
        memberInfo.append("会员到期日期:").append("\n").append(dateExpireFormat).append("\n\n");
        memberInfo.append("AI绘图剩余次数:").append(imageNum).append("次").append("\n\n");
        memberInfo.append("ChatGpt剩余次数:").append("\n").append("无限制");
        return memberInfo.toString();
    }
}
