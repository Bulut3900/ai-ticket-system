package com.example.aiservice.service;

import com.example.aiservice.dto.TicketCreateRequest;
import com.example.aiservice.dto.TicketUpdateStatusRequest;
import com.example.aiservice.entity.Ticket;

import java.util.List;

public interface TicketService {

    /**
     * 创建工单（内部会调用 AI 分析）
     */
    Ticket create(TicketCreateRequest request, Long creatorId);

    /**
     * 查询工单列表（普通用户只能看自己的，管理员能看全部）
     */
    List<Ticket> list(Long userId, String role);

    /**
     * 查询工单详情
     */
    Ticket detail(Long id, Long userId, String role);

    /**
     * 更新工单状态（仅管理员）
     */
    void updateStatus(Long id, TicketUpdateStatusRequest request, String role);

    /**
     * 删除工单
     */
    void delete(Long id, Long userId, String role);
}