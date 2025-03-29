package com.newket.infra.mongodb.ticket_buffer.repository

import com.newket.infra.mongodb.ticket_buffer.entity.TicketSaleBuffer
import org.springframework.data.mongodb.repository.MongoRepository

interface TicketSaleBufferRepository : MongoRepository<TicketSaleBuffer, Long> {
}