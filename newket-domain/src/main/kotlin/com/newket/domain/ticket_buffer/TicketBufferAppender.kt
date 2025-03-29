package com.newket.domain.ticket_buffer

import com.newket.infra.mongodb.ticket_buffer.entity.TicketArtistBuffer
import com.newket.infra.mongodb.ticket_buffer.entity.TicketBuffer
import com.newket.infra.mongodb.ticket_buffer.entity.TicketSaleBuffer
import com.newket.infra.mongodb.ticket_buffer.repository.TicketArtistBufferRepository
import com.newket.infra.mongodb.ticket_buffer.repository.TicketBufferRepository
import com.newket.infra.mongodb.ticket_buffer.repository.TicketSaleBufferRepository
import org.springframework.stereotype.Service

@Service
class TicketBufferAppender(
    private val ticketBufferRepository: TicketBufferRepository,
    private val ticketSaleBufferRepository: TicketSaleBufferRepository,
    private val ticketArtistBufferRepository: TicketArtistBufferRepository
) {
    fun saveTicketBuffer(ticketBuffer: TicketBuffer) = ticketBufferRepository.save(ticketBuffer)

    fun saveTicketSaleBuffer(ticketSaleBuffer: TicketSaleBuffer) = ticketSaleBufferRepository.save(ticketSaleBuffer)

    fun saveTicketArtistBuffer(ticketArtistBuffer: TicketArtistBuffer) = ticketArtistBufferRepository.save(ticketArtistBuffer)
}