package com.newket.application.ticket.dto

class OnSale {
    data class Response(
        val totalNum: Int,
        val concerts: List<Concert>
    )

    data class Concert(
        val concertId: Long,
        val imageUrl: String,
        val title: String,
        val date: String
    )
}