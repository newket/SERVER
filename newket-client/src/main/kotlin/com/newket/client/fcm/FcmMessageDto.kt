package com.newket.client.fcm

data class FcmMessageDto(
    val message: Message
){
    data class Message(
        val token: String,
        val notification: Notification,
        val data: Map<String, String>
    )

    data class Notification(
        val title: String,
        val body: String
    )
}

