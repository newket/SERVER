package com.newket.domain.ticket_buffer

import com.newket.infra.mongodb.ticket_buffer.repository.TicketBufferRepository
import org.springframework.stereotype.Service

@Service
class TicketBufferRemover(
    private val ticketBufferRepository: TicketBufferRepository
) {
    fun deleteAllTicketBuffer() = ticketBufferRepository.deleteAll()

    fun deleteByTicketId(ticketId: Long) = ticketBufferRepository.deleteByTicketId(ticketId)
}