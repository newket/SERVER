package com.newket.client.crawling

import com.newket.infra.jpa.ticket.constant.Genre

data class CreateMusicalRequest(
    val genre: Genre,
    val artists: List<Artist>,
    val place: String?,
    val title: String,
    val imageUrl: String,
    val ticketEventSchedule: List<CreateTicketRequest.TicketEventSchedule>,
    val ticketSaleUrls: List<CreateTicketRequest.TicketSaleUrl>,
    val lineupImage: String?,
    val price: List<CreateTicketRequest.Price>
) {
    data class Artist(
        val artistId: Long,
        val name: String,
        val role: String,
    )
}
