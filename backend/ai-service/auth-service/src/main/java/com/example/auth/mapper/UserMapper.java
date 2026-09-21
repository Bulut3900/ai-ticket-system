package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.entity.User;      // ← 改成 auth
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}