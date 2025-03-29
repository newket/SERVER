package com.newket.infra.mongodb.ticket_cache.entity

import com.newket.infra.jpa.ticket.constant.Genre
import com.newket.infra.mongodb.config.BaseTimeMongoEntity
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "ticket_cache")
data class TicketCache(
    val ticketId: Long,
    val genre: Genre,
    val imageUrl: String,
    val title: String,
    val customDate: String,
    val ticketEventSchedules: List<TicketEventSchedule>,
    var ticketSaleSchedules: List<TicketSaleSchedule>,
    var artists: List<Artist>,
) : BaseTimeMongoEntity() {
    fun updateTicketSaleSchedules(ticketSaleSchedules: List<TicketSaleSchedule>) {
        this.ticketSaleSchedules = ticketSaleSchedules
    }

    fun updateArtists(artists: List<Artist>) {
        this.artists = artists
    }
}

data class TicketEventSchedule(
    val dateTime: LocalDateTime,
    val customDateTime: String,
)

data class TicketSaleSchedule(
    val type: String,
    val dateTime: LocalDateTime,
)

data class Artist(
    val artistId: Long,
    val name: String,
    val subName: String?,
    val nickname: String?,
)