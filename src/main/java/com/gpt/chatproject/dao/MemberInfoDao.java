package com.gpt.chatproject.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gpt.chatproject.entity.MemberInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MemberInfoDao extends BaseMapper<MemberInfo> {
    int insert(MemberInfo memberInfo);

    int update(MemberInfo memberInfo);

    int delete(Integer userId);

    MemberInfo findByUserId(String userId);

    List<MemberInfo> findAll();
}
