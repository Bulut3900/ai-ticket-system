package com.example.aiservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.aiservice.entity.Ticket;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TicketMapper extends BaseMapper<Ticket> {
}