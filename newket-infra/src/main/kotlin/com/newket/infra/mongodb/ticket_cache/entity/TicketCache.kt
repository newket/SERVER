package com.newket.infra.mongodb.ticket_cache.entity

import com.newket.infra.jpa.ticket.constant.Genre
import com.newket.infra.jpa.ticket.constant.TicketProvider
import com.newket.infra.mongodb.config.BaseTimeMongoEntity
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "ticket_cache")
data class TicketCache(
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

data class TicketEventSchedule(
    val dateTime: LocalDateTime,
    val customDateTime: String,
)

data class TicketSaleSchedule(
    val type: String,
    val dateTime: LocalDateTime,
    val customDateTime: String,
    val ticketSaleUrls: List<TicketSaleUrl>
)

data class TicketSaleUrl(
    val ticketProvider: TicketProvider,
    val url: String
)

data class TicketPrice(
    val type: String,
    val price: String
)

data class LineupImage(
    val message: String,
    val imageUrl: String
)

data class Artist(
    val artistId: Long,
    val name: String,
    val subName: String?,
    val nickname: String?,
    val role: String?,
    val imageUrl: String?
)