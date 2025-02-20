package com.newket.application.user.dto

class NotificationAllow {
    data class Response (
        val artistNotification : Boolean,
        val ticketNotification : Boolean,
    )
}