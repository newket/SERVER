package com.newket.application.admin.dto

import com.newket.client.crawling.CreateTicketRequest

data class AddTicketArtistsRequest (
    val artists: List<CreateTicketRequest.Artist>
)