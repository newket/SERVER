package com.newket.application.ticket.dto

class Search {
    data class Response(
        val artists: List<Artist>,
        val openingNotice : OpeningNotice.Response,
        val onSale: OnSale.Response
    )

    data class Artist(
        val name: String,
        val nicknames: String?,
        val artistId: Long
    )
}