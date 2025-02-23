package com.newket.domain.ticket_buffer

import com.newket.infra.mongodb.ticket_buffer.entity.TicketBuffer
import com.newket.infra.mongodb.ticket_buffer.repository.TicketBufferRepository
import org.springframework.stereotype.Service

@Service
class TicketBufferAppender(
    private val ticketBufferRepository: TicketBufferRepository
) {
    fun saveTicketBuffer(ticketBuffer: TicketBuffer) = ticketBufferRepository.save(ticketBuffer)

    fun saveAllTicketBuffer(ticketBuffers: List<TicketBuffer>) = ticketBufferRepository.saveAll(ticketBuffers)
}