package com.newket.api.controller.notification

object NotificationApi {
    object V1 {
        const val BASE_URL="/api/v1/notifications"
        const val OPEN="$BASE_URL/ticket-open"
        const val OPEN_ALL="$OPEN/all"
        const val OPENED="$BASE_URL/opened"
    }
}