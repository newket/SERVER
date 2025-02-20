package com.newket.api.controller.notification

import com.newket.application.notification.NotificationService
import com.newket.application.notification.dto.ConcertIds
import com.newket.application.notification.dto.Notifications
import com.newket.application.ticket.dto.OpeningNotice
import org.springframework.web.bind.annotation.*

@RestController
class NotificationController(
    private val notificationService: NotificationService
){
    @PostMapping(NotificationApi.V1.OPEN)
    fun addTicketNotification(@RequestParam concertId: Long) {
        return notificationService.addTicketNotification(concertId)
    }

    @GetMapping(NotificationApi.V1.OPEN)
    fun getIsTicketNotification(@RequestParam concertId: Long) : Boolean {
        return notificationService.getIsTicketNotification(concertId)
    }

    @DeleteMapping(NotificationApi.V1.OPEN)
    fun deleteTicketNotification(@RequestParam concertId: Long) {
        return notificationService.deleteTicketNotification(concertId)
    }

    @GetMapping(NotificationApi.V1.OPEN_ALL)
    fun getAllTicketNotifications() : OpeningNotice.Response {
        return notificationService.getAllTicketNotification()
    }

    @DeleteMapping(NotificationApi.V1.OPEN_ALL)
    fun deleteAllTicketNotifications(@RequestBody request: ConcertIds.Request) {
        return notificationService.deleteAllTicketNotifications(request)
    }

    @PostMapping(NotificationApi.V1.OPENED)
    fun updateNotificationIsOpened(@RequestParam notificationId: Long) {
        return notificationService.updateNotificationIsOpened(notificationId)
    }

    @GetMapping(NotificationApi.V1.BASE_URL)
    fun getAllNotifications() : Notifications.Response{
        return notificationService.getAllNotifications()
    }
}
