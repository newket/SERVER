package com.newket.domain.ticket_buffer

import com.newket.infra.jpa.ticket.constant.Genre
import com.newket.infra.mongodb.ticket_buffer.repository.TicketArtistBufferRepository
import com.newket.infra.mongodb.ticket_buffer.repository.TicketBufferRepository
import com.newket.infra.mongodb.ticket_buffer.repository.TicketSaleBufferRepository
import org.springframework.stereotype.Service

@Service
class TicketBufferReader(
    private val ticketBufferRepository: TicketBufferRepository,
    private val ticketSaleBufferRepository: TicketSaleBufferRepository,
    private val ticketArtistBufferRepository: TicketArtistBufferRepository
) {
    fun findAllTicketBuffer() = ticketBufferRepository.findAll()

    fun findAllTicketBufferByGenre(genre: Genre) = ticketBufferRepository.findAllByGenreOrderByTicketIdDesc(genre)

    fun findAllTicketSaleBuffer() = ticketSaleBufferRepository.findAll()

    fun findAllTicketArtistBuffer() = ticketArtistBufferRepository.findAll()
}