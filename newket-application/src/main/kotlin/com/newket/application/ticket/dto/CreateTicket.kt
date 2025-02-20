package com.newket.application.ticket.dto

import com.newket.infra.jpa.ticket.constant.Genre
import com.newket.infra.jpa.ticket.constant.TicketProvider
import java.time.LocalDate
import java.time.LocalTime

class CreateTicket {
    data class Request(
        val genre: Genre,
        val artist: List<String>,
        val place: String,
        val title: String,
        val imageUrl: String,
        val ticketEventSchedule: List<TicketEventSchedule>,
        val ticketSaleUrls: List<TicketSaleUrls>,
        val lineupImage: String?,
        val price: List<TicketDetail.V1.Price>
    )

    data class TicketSaleUrls(
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
}