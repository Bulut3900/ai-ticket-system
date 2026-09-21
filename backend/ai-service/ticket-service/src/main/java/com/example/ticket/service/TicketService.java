package com.example.ticket.service;

import com.example.ticket.dto.TicketCreateRequest;
import com.example.ticket.dto.TicketUpdateStatusRequest;
import com.example.ticket.entity.Ticket;

import java.util.List;

public interface TicketService {

    Ticket create(TicketCreateRequest request, Long creatorId);

    List<Ticket> list(Long userId, String role);

    Ticket detail(Long id, Long userId, String role);

    void updateStatus(Long id, TicketUpdateStatusRequest request, String role);

    void delete(Long id, Long userId, String role);
}