package com.newket.application.admin.dto

data class TicketTableResponse(
    val ticketId: Long?,
    val title: String,
    val place: String,
    val dateList: List<String>,
    val ticketSaleSchedules: List<TicketSaleScheduleDto>,
    val prices: List<PriceDto>,
    val artists: List<String>,
) {
    data class TicketSaleScheduleDto(
        val type: String,
        val date: String,
        val ticketProviders: List<String>
    )

    data class PriceDto(
        val type: String,
        val price: String
    )
}