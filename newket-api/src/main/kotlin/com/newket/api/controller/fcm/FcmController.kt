package com.newket.api.controller.fcm

import com.newket.application.fcm.FcmService
import com.newket.application.fcm.dto.FcmSendDto
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.io.IOException

@RestController
class FcmController(
    private val fcmService: FcmService
) {
    @PostMapping(FcmApi.V1.SEND)
    @Throws(IOException::class)
    fun pushMessage(
        @RequestBody fcmSendDto: FcmSendDto
    ): String {
        return fcmService.sendMessageTo(fcmSendDto)
    }
}