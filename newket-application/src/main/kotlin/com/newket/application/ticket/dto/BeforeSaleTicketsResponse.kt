package com.newket.application.ticket.dto

data class BeforeSaleTicketsResponse(
    val totalNum: Int,
    val tickets: List<BeforeSaleTicketDto>
) {
    data class BeforeSaleTicketDto(
        val ticketId: Long,
        val imageUrl: String,
        val title: String,
        val ticketSaleSchedules: List<TicketSaleScheduleDto>
    )

    data class TicketSaleScheduleDto(
        val type: String,
        val dDay: String
    )
}