package com.gpt.chatproject.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gpt.chatproject.entity.Fans;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FansDao extends BaseMapper<Fans> {
    void saveFans(Fans fans);

    void updateFans(Fans fans);

    void deleteFans(String userId);

    Fans getFansByUserId(String userId);
}
