package com.newket.domain.ticket_buffer

import com.newket.infra.mongodb.ticket_buffer.repository.TicketBufferRepository
import org.springframework.stereotype.Service

@Service
class TicketBufferReader(
    private val ticketBufferRepository: TicketBufferRepository
) {
    fun findAllTicketBuffer() = ticketBufferRepository.findAll()
}