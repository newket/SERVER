package com.newket.client.crawling

import com.newket.infra.jpa.ticket.constant.Genre
import com.newket.infra.jpa.ticket.constant.TicketProvider
import java.time.LocalDate
import java.time.LocalTime

data class CreateTicketRequest(
    val genre: Genre,
    val artists: List<Artist>,
    val place: String?,
    val title: String,
    val imageUrl: String,
    val ticketEventSchedule: List<TicketEventSchedule>,
    val ticketSaleUrls: List<TicketSaleUrl>,
    val lineupImage: String?,
    val price: List<Price>
) {
    data class Artist(
        val artistId: Long,
        val name: String,
    )
    data class TicketSaleUrl(
        val ticketProvider: TicketProvider,
        val url: String,
        val isDirectUrl: Boolean,
        val ticketSaleSchedules: List<TicketSaleSchedule>,
    )

    data class TicketEventSchedule(
        val day: LocalDate,
        val time: LocalTime
    )

    data class TicketSaleSchedule(
        val day: LocalDate,
        val time: LocalTime,
        val type: String
    )

    data class Price(
        val type: String,
        val price: String
    )
}