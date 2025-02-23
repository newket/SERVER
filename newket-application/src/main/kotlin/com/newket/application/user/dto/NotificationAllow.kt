package com.newket.application.user.dto

class NotificationAllow {
    data class Request(
        val isAllow: String,
        val target: String,
        val token: String
    )
    data class Response (
        val artistNotification : Boolean,
        val ticketNotification : Boolean,
    )
}