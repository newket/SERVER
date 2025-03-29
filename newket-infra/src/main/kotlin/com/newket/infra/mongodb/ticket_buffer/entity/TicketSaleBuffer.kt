package com.newket.infra.mongodb.ticket_buffer.entity

import com.newket.infra.mongodb.config.BaseTimeMongoEntity
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "ticket_sale_buffer")
class TicketSaleBuffer(
    val ticketId: Long,
    val ticketSaleUrlId: Long,
    val dateTime: LocalDateTime,
    val type: String
) : BaseTimeMongoEntity()