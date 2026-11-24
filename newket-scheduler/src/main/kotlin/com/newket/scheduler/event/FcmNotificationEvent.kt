package com.newket.scheduler.event

data class FcmNotificationEvent(
    val token: String,
    val title: String,
    val body: String,
    val ticketId: String,
    val notificationId: String
)