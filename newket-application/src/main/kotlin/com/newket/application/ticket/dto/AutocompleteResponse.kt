package com.newket.application.ticket.dto

data class AutocompleteResponse(
    val artists: List<Artist>,
    val tickets: List<Ticket>
) {
    data class Artist(
        val artistId: Long,
        val name: String,
        val subName: String?
    )

    data class Ticket(
        val ticketId: Long,
        val title: String
    )
}