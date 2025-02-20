package com.newket.application.ticket.dto

class SearchResult {
    data class Response(
        val artists: List<Artist>,
        val openingNotice : OpeningNotice.Response,
        val onSale: OnSale.Response
    )

    data class Artist(
        val artistId: Long,
        val name: String,
        val subName: String?,
        val imageUrl: String?
    )

    class OpeningNotice {
        data class Response(
            val totalNum: Int,
            val concerts: List<Concert>
        )

        data class Concert(
            val concertId: Long,
            val imageUrl: String,
            val title: String,
            val ticketingSchedules: List<ConcertTicketingSchedule>
        )

        data class ConcertTicketingSchedule(
            val type: String,
            val dDay: String
        )
    }
}