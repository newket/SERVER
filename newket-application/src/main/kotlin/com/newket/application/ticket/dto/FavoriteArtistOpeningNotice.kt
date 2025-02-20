package com.newket.application.ticket.dto

class FavoriteArtistOpeningNotice {
    data class Response(
        val totalNum: Int,
        val artistName: String,
        val favoriteArtistNames: List<String>,
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