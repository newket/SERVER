package com.newket.application.admin.dto

import com.newket.client.crawling.CreateTicketRequest

data class AddTicketArtistsRequest (
    val ticketId: Long,
    val artists: List<CreateTicketRequest.Artist>
)