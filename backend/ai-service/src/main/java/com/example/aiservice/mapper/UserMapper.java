package com.example.aiservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.aiservice.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}