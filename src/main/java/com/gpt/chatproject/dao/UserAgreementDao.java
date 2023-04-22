package com.gpt.chatproject.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gpt.chatproject.entity.UserAgreement;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserAgreementDao extends BaseMapper<UserAgreement> {
    int insert(UserAgreement userAgreement);

    int update(UserAgreement userAgreement);

    int delete(int id);

    UserAgreement findById(int id);

    List<UserAgreement> findAll();
}
