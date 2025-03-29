package com.newket.domain.ticket_buffer

import com.newket.infra.mongodb.ticket_buffer.repository.TicketArtistBufferRepository
import com.newket.infra.mongodb.ticket_buffer.repository.TicketBufferRepository
import com.newket.infra.mongodb.ticket_buffer.repository.TicketSaleBufferRepository
import org.springframework.stereotype.Service

@Service
class TicketBufferRemover(
    private val ticketBufferRepository: TicketBufferRepository,
    private val ticketSaleBufferRepository: TicketSaleBufferRepository,
    private val ticketArtistBufferRepository: TicketArtistBufferRepository
) {
    fun deleteAllTicketBuffer() = ticketBufferRepository.deleteAll()

    fun deleteByTicketId(ticketId: Long) = ticketBufferRepository.deleteByTicketId(ticketId)

    fun deleteAllTicketSaleBuffer() = ticketSaleBufferRepository.deleteAll()

    fun deleteAllTicketArtistBuffer() = ticketArtistBufferRepository.deleteAll()
}