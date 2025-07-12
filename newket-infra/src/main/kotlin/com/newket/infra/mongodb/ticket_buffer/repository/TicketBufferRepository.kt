package com.newket.infra.mongodb.ticket_buffer.repository

import com.newket.infra.jpa.ticket.constant.Genre
import com.newket.infra.mongodb.ticket_buffer.entity.TicketBuffer
import org.springframework.data.mongodb.repository.MongoRepository

interface TicketBufferRepository : MongoRepository<TicketBuffer, String> {
    fun deleteByTicketId(ticketId: Long)

    fun findAllByGenreOrderByTicketIdDesc(genre: Genre): List<TicketBuffer>
}