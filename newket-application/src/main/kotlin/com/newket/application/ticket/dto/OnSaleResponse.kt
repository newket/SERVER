package com.newket.application.ticket.dto

data class OnSaleResponse(
    val totalNum: Int,
    val tickets: List<OnSaleTicketDto>
) {
    data class OnSaleTicketDto(
        val ticketId: Long,
        val imageUrl: String,
        val title: String,
        val date: String
    )
}