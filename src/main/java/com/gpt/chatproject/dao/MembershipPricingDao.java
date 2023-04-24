package com.gpt.chatproject.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gpt.chatproject.entity.MembershipPricing;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MembershipPricingDao extends BaseMapper<MembershipPricing> {
    int insert(MembershipPricing membershipPricing);

    int update(MembershipPricing membershipPricing);

    int delete(int id);

    MembershipPricing findById(int id);

    List<MembershipPricing> findAll();

    List<MembershipPricing> findExcludeFirstOrder();
}
