package com.newket.api.controller.ticket

object TicketApi {

    object V1 {
        const val BASE_URL = "/api/v1/tickets"
        const val TICKET_DETAIL = "${BASE_URL}/{ticketId}"
        const val BEFORE_SALE = "${BASE_URL}/before-sale"
        const val ON_SALE = "${BASE_URL}/on-sale"
        const val SEARCH = "${BASE_URL}/search"
        const val AUTOCOMPLETE = "${BASE_URL}/autocomplete"
        const val FETCH = "${BASE_URL}/fetch"
    }
}