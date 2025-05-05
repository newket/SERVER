package com.newket.application.admin.dto

import com.newket.application.artist.dto.common.ArtistDto

data class TicketTableResponse(
    val ticketId: Long?,
    val title: String,
    val place: String,
    val date: String,
    val dateList: List<String>,
    val ticketSaleSchedules: List<TicketSaleScheduleDto>,
    val prices: List<PriceDto>,
    val artists: List<ArtistDto>,
){
    data class TicketSaleScheduleDto(
        val type: String,
        val date: String,
        val ticketSaleUrls: List<TicketSaleUrlDto>
    )

    data class TicketSaleUrlDto(
        val providerImageUrl: String,
        val ticketProvider: String,
        val url: String,
    )

    data class PriceDto(
        val type: String,
        val price: String
    )
}