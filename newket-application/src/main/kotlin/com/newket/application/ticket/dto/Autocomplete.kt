package com.newket.application.ticket.dto

class Autocomplete {
    data class Response(
        val artists: List<Artist>,
        val tickets: List<Ticket>
    )

    data class Artist(
        val artistId: Long,
        val name: String,
        val subName: String?
    )

    data class Ticket(
        val concertId: Long,
        val title: String
    )
}