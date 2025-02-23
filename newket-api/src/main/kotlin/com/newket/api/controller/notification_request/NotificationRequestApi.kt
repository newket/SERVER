package com.newket.api.controller.notification_request

object NotificationRequestApi {
    object V1 {
        const val BASE_URL = "/api/v1/notification-requests"
        const val ARTIST = "$BASE_URL/artists"
        const val ARTIST_DETAIL = "$ARTIST/{artistId}"
        const val TICKET = "$BASE_URL/tickets"
        const val TICKET_DETAIL = "$TICKET/{ticketId}"
        const val ARTIST_BEFORE_SALE = "$ARTIST/before-sale"
        const val ARTIST_ON_SALE = "$ARTIST/on-sale"
    }
}