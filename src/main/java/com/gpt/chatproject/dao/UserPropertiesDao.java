package com.gpt.chatproject.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gpt.chatproject.entity.UserProperties;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserPropertiesDao extends BaseMapper<UserProperties> {
}
