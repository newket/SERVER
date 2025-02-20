package com.newket.api.controller.ticket

object TicketApi {

    object V1 {
        const val BASE_URL = "/api/v1/tickets"
        const val TICKET_DETAIL = "${BASE_URL}/{ticketId}"
        const val OPEN = "${BASE_URL}/open"
        const val ON_SALE = "${BASE_URL}/onsale"
        const val SEARCH = "${BASE_URL}/search"
        const val FAVORITE = "${BASE_URL}/favorite"
        const val AUTOCOMPLETE = "${BASE_URL}/autocomplete"
    }

    object V2 {
        const val BASE_URL = "/api/v2/tickets"
        const val TICKET_DETAIL = "${BASE_URL}/{concertId}"
        const val SEARCH = "${BASE_URL}/search"
    }
}