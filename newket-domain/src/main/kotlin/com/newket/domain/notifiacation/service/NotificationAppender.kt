package com.newket.domain.notifiacation.service

import com.newket.infra.jpa.notifiacation.entity.Notification
import com.newket.infra.jpa.notifiacation.entity.TicketNotification
import com.newket.infra.jpa.notifiacation.repository.NotificationRepository
import com.newket.infra.jpa.notifiacation.repository.TicketNotificationRepository
import org.springframework.stereotype.Service

@Service
class NotificationAppender(
    private val ticketNotificationRepository: TicketNotificationRepository,
    private val notificationRepository: NotificationRepository
) {
    fun addUserFavoriteTicket(ticketNotification: TicketNotification) =
        ticketNotificationRepository.save(ticketNotification)

    fun addNotification(notification: Notification) = notificationRepository.save(notification)
}