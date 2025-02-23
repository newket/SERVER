package com.newket.application.artist.dto

import com.newket.application.artist.dto.common.ArtistDto
import com.newket.application.ticket.dto.BeforeSaleTicketsResponse
import com.newket.application.ticket.dto.OnSaleResponse

data class ArtistProfileResponse(
    val info: ArtistDto,
    val members: List<ArtistDto>,
    val groups: List<ArtistDto>,
    val beforeSaleTickets: BeforeSaleTicketsResponse,
    val onSaleTickets: OnSaleResponse,
    val afterSaleTickets: OnSaleResponse
)