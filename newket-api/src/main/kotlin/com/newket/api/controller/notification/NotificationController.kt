package com.newket.api.controller.notification

import com.newket.application.notification.NotificationService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class NotificationController(
    private val notificationService: NotificationService
){
    @PostMapping(NotificationApi.V1.OPENED)
    fun updateNotificationIsOpened(@RequestParam notificationId: Long) {
        return notificationService.updateNotificationIsOpened(notificationId)
    }
}
