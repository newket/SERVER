package com.newket.infra.mongodb.ticket_buffer.repository

import com.newket.infra.mongodb.ticket_buffer.entity.TicketArtistBuffer
import org.springframework.data.mongodb.repository.MongoRepository

interface TicketArtistBufferRepository : MongoRepository<TicketArtistBuffer, Long> {
}