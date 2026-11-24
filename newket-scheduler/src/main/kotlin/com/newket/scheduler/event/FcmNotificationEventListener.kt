package com.newket.scheduler.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.newket.client.fcm.FcmClient
import com.newket.client.fcm.FcmMessageDto
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class FcmNotificationEventListener(
    private val fcmClient: FcmClient,
    private val objectMapper: ObjectMapper
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: FcmNotificationEvent) {
        val fcmMessageDto = FcmMessageDto(
            message = FcmMessageDto.Message(
                token = event.token,
                notification = FcmMessageDto.Notification(
                    title = event.title,
                    body = event.body
                ),
                data = mapOf(
                    "ticketId" to event.ticketId,
                    "notificationId" to event.notificationId
                )
            )
        )

        fcmClient.sendMessage(
            objectMapper.writeValueAsString(fcmMessageDto)
        )
    }
}