package com.newket.application.notification.dto

class Notifications {
    data class Response(
        val notifications: List<Notification>
    )

    data class Notification(
        val title: String,
        val content: String
    )
}