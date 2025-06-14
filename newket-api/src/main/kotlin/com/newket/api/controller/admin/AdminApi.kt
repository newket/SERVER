package com.newket.api.controller.admin

object AdminApi {

    object V1 {
        const val BASE_URL = "/api/v1/admins"

        const val TICKET = "${BASE_URL}/ticket"
        const val TICKET_FETCH = "${TICKET}/fetch"
        const val TICKET_BUFFER = "${TICKET}/buffer"
        const val TICKET_SALE = "${TICKET}/ticket-sale"
        const val TICKET_ARTIST = "${TICKET}/artist"
        const val TICKET_DETAIL = "${TICKET}/{ticketId}"

        const val TICKET_MUSICAL = "${TICKET}/musical"
        const val TICKET_FETCH_MUSICAL = "${TICKET_MUSICAL}/fetch"
        const val TICKET_MUSICAL_DETAIL = "${TICKET_MUSICAL}/{ticketId}"

        const val ARTIST = "${BASE_URL}/artist"
        const val ARTIST_FETCH = "${ARTIST}/fetch"
        const val ARTIST_SEARCH = "${ARTIST}/search"

        const val PLACE = "${BASE_URL}/place"
    }
}