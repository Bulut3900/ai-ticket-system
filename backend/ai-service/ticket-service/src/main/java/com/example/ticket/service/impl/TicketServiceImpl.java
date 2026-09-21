package com.example.ticket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.BusinessException;
import com.example.ticket.dto.TicketAnalysis;
import com.example.ticket.dto.TicketCreateRequest;
import com.example.ticket.dto.TicketUpdateStatusRequest;
import com.example.ticket.entity.Ticket;
import com.example.ticket.feign.AiServiceClient;
import com.example.ticket.mapper.TicketMapper;
import com.example.ticket.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketMapper ticketMapper;
    private final AiServiceClient aiServiceClient;

    @Override
    public Ticket create(TicketCreateRequest request, Long creatorId) {
        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle());
        ticket.setContent(request.getContent());
        ticket.setCreatorId(creatorId);
        ticket.setStatus("PENDING");

        // ============ AI 分析（通过 Feign 调用 ai-service） ============
        try {
            TicketAnalysis analysis = aiServiceClient.analyze(
                    request.getTitle() + "：" + request.getContent()
            );

            ticket.setSummary(analysis.summary());
            ticket.setCategory(analysis.category());
            ticket.setPriority(analysis.priority());

            log.info("AI 分析工单成功: summary={}, category={}, priority={}",
                    analysis.summary(), analysis.category(), analysis.priority());
        } catch (Exception e) {
            // AI 失败时兜底，保证工单能正常创建
            log.error("AI 分析工单失败，使用默认值", e);
            ticket.setSummary(request.getTitle());
            ticket.setCategory("其他");
            ticket.setPriority("中");
        }
        // ==============================================================

        ticketMapper.insert(ticket);
        return ticket;
    }

    @Override
    public List<Ticket> list(Long userId, String role) {
        LambdaQueryWrapper<Ticket> wrapper = new LambdaQueryWrapper<>();
        // 普通用户只能看自己创建的
        if (!"ADMIN".equals(role)) {
            wrapper.eq(Ticket::getCreatorId, userId);
        }
        wrapper.orderByDesc(Ticket::getCreateTime);
        return ticketMapper.selectList(wrapper);
    }

    @Override
    public Ticket detail(Long id, Long userId, String role) {
        Ticket ticket = ticketMapper.selectById(id);
        if (ticket == null) {
            throw new BusinessException("工单不存在");
        }
        // 权限校验：普通用户只能看自己的
        if (!"ADMIN".equals(role) && !ticket.getCreatorId().equals(userId)) {
            throw new BusinessException(403, "无权访问该工单");
        }
        return ticket;
    }

    @Override
    public void updateStatus(Long id, TicketUpdateStatusRequest request, String role) {
        // 只有管理员能改状态
        if (!"ADMIN".equals(role)) {
            throw new BusinessException(403, "无权修改工单状态");
        }

        Ticket ticket = ticketMapper.selectById(id);
        if (ticket == null) {
            throw new BusinessException("工单不存在");
        }

        // 状态合法性校验
        List<String> validStatus = List.of("PENDING", "PROCESSING", "RESOLVED", "CLOSED");
        if (!validStatus.contains(request.getStatus())) {
            throw new BusinessException("非法的工单状态");
        }

        ticket.setStatus(request.getStatus());
        ticketMapper.updateById(ticket);
    }

    @Override
    public void delete(Long id, Long userId, String role) {
        Ticket ticket = ticketMapper.selectById(id);
        if (ticket == null) {
            throw new BusinessException("工单不存在");
        }
        // 普通用户只能删自己的
        if (!"ADMIN".equals(role) && !ticket.getCreatorId().equals(userId)) {
            throw new BusinessException(403, "无权删除该工单");
        }
        ticketMapper.deleteById(id);
    }
}