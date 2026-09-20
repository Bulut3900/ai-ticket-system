package com.example.aiservice.controller;

import com.example.aiservice.common.Result;
import com.example.aiservice.dto.TicketCreateRequest;
import com.example.aiservice.dto.TicketUpdateStatusRequest;
import com.example.aiservice.entity.Ticket;
import com.example.aiservice.service.TicketService;
import com.example.aiservice.util.UserContext;
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