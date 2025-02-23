package com.newket.application.ticket.dto

import com.newket.application.artist.dto.common.ArtistDto

data class TicketDetailResponse(
    val imageUrl: String,
    val title: String,
    val place: String,
    val placeUrl: String,
    val date: String,
    val dateList: List<String>,
    val ticketSaleSchedules: List<TicketSaleScheduleDto>,
    val prices: List<PriceDto>,
    val lineup: LineupImageDto?,
    val artists: List<ArtistDto>,
    val isAvailableNotification: Boolean,
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

    data class LineupImageDto(
        val message: String,
        val imageUrl: String,
    )
}