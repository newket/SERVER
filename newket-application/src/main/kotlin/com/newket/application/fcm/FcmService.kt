package com.newket.application.fcm

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.newket.client.fcm.FcmMessageDto
import com.newket.application.fcm.dto.FcmSendDto
import com.newket.client.fcm.FcmClient
import org.springframework.http.*
import org.springframework.stereotype.Service
import java.io.IOException

@Service
class FcmService (
    private val fcmClient: FcmClient
){
    @Throws(IOException::class)
    fun sendMessageTo(fcmSendDto: FcmSendDto): String {
        val message = makeMessage(fcmSendDto)
        val response = fcmClient.sendMessage(message)
        return if (response?.statusCode === HttpStatus.OK) "성공" else "실패"
    }

    @Throws(JsonProcessingException::class)
    private fun makeMessage(fcmSendDto: FcmSendDto): String {
        val om = ObjectMapper()
        val fcmMessageDto = FcmMessageDto(
            message = FcmMessageDto.Message(
                token = fcmSendDto.token,
                notification = FcmMessageDto.Notification(
                    title = fcmSendDto.title,
                    body = fcmSendDto.body,
                ),
                data = mapOf(
                    "notificationId" to fcmSendDto.payload
                )
            )
        )
        return om.writeValueAsString(fcmMessageDto)
    }
}