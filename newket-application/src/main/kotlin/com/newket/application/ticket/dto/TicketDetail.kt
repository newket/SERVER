package com.newket.application.ticket.dto

import com.newket.infra.jpa.ticket.constant.TicketProvider
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class TicketDetail {
    object V1 {
        data class Response(
            val imageUrl: String,
            val title: String,
            val place: String,
            val placeUrl: String,
            val date: String,
            val dateList: List<String>,
            val ticketingSchedules: List<ConcertTicketingSchedule>,
            val prices: List<Price>,
            val lineup: LineupImage?,
            val artists: List<Artist>,
            val isAvailableNotification: Boolean,
        )

        data class ConcertTicketProvider(
            val ticketProvider: TicketProvider,
            val url: String,
        )

        data class ConcertTicketingSchedule(
            val type: String,
            val date: String,
            val ticketProviders: List<ConcertTicketProvider>
        )

        data class Price(
            val type: String,
            val price: String
        )

        data class LineupImage(
            val message: String,
            val imageUrl: String,
        )

        data class Artist(
            val artistId: Long,
            val name: String,
            val subName: String?,
            val imageUrl: String?
        )
    }

    object V2 {
        data class Response(
            val imageUrl: String,
            val title: String,
            val place: String,
            val placeUrl: String,
            val date: List<String>,
            val ticketProviders: List<ConcertTicketProvider>,
            val artists: List<Artist>,
            val isAvailableNotification: Boolean
        )

        data class ConcertTicketProvider(
            val ticketProvider: TicketProvider,
            val url: String,
            val ticketingSchedules: List<ConcertTicketingSchedule>
        )

        data class ConcertTicketingSchedule(
            val type: String,
            val date: String,
            val time: String,
            val dDay: String
        )

        data class Artist(
            val artistId: Long,
            val name: String,
            val nicknames: String?
        )
    }

    fun timeToString(time: LocalTime): String {
        return time.format(DateTimeFormatter.ofPattern("HH:mm"))
    }
}