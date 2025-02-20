package com.newket.application.fcm.dto

data class FcmSendDto(
    val token: String,
    val title: String,
    val body: String,
    val payload: String
)
