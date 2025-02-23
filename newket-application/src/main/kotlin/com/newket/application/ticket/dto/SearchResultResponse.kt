package com.newket.application.ticket.dto

import com.newket.application.artist.dto.common.ArtistDto

data class SearchResultResponse(
    val artists: List<ArtistDto>,
    val beforeSaleTickets: BeforeSaleTicketsResponse,
    val onSaleTickets: OnSaleResponse
)