package com.newket.infra.mongodb.ticket_buffer.entity

import com.newket.infra.jpa.ticket.constant.Genre
import com.newket.infra.mongodb.config.BaseTimeMongoEntity
import com.newket.infra.mongodb.ticket_cache.entity.*
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "ticket_buffer")
data class TicketBuffer(
    val ticketId: Long,
    val genre: Genre,
    val imageUrl: String,
    val title: String,
    val place: String,
    val placeUrl: String,
    val customDate: String,
    val ticketEventSchedules: List<TicketEventSchedule>,
    val ticketSaleSchedules: List<TicketSaleSchedule>,
    val prices: List<TicketPrice>,
    val lineupImage: LineupImage?,
    val artists: List<Artist>,
) : BaseTimeMongoEntity()