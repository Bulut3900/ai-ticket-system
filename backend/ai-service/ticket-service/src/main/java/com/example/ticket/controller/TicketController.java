package com.example.ticket.controller;

import com.example.common.Result;
import com.example.ticket.dto.TicketCreateRequest;
import com.example.ticket.dto.TicketUpdateStatusRequest;
import com.example.ticket.entity.Ticket;
import com.example.ticket.service.TicketService;
import com.example.common.util.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ticket")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    /**
     * 创建工单（会自动调用 AI 分析）
     */
    @PostMapping
    public Result<Ticket> create(@Valid @RequestBody TicketCreateRequest request) {
        Long userId = UserContext.getUserId();
        return Result.success(ticketService.create(request, userId));
    }

    /**
     * 查询工单列表
     */
    @GetMapping
    public Result<List<Ticket>> list() {
        Long userId = UserContext.getUserId();
        String role = UserContext.getRole();
        return Result.success(ticketService.list(userId, role));
    }

    /**
     * 查询工单详情
     */
    @GetMapping("/{id}")
    public Result<Ticket> detail(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        String role = UserContext.getRole();
        return Result.success(ticketService.detail(id, userId, role));
    }

    /**
     * 更新工单状态（仅管理员）
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @Valid @RequestBody TicketUpdateStatusRequest request) {
        String role = UserContext.getRole();
        ticketService.updateStatus(id, request, role);
        return Result.success();
    }

    /**
     * 删除工单
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        String role = UserContext.getRole();
        ticketService.delete(id, userId, role);
        return Result.success();
    }
}