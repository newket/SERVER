package com.newket.domain.notification

import com.newket.infra.jpa.notifiacation.entity.Notification
import com.newket.infra.jpa.notification_request.entity.TicketNotification
import com.newket.infra.jpa.notifiacation.repository.NotificationRepository
import com.newket.infra.jpa.notification_request.repository.TicketNotificationRepository
import org.springframework.stereotype.Service

@Service
class NotificationReader(
    private val ticketNotificationRepository: TicketNotificationRepository,
    private val notificationRepository: NotificationRepository
) {

    fun findAllTicketNotificationByTicketId(ticketId: Long): List<TicketNotification> {
        return ticketNotificationRepository.findAllByTicketId(ticketId)
    }

    fun findById(notificationId: Long): Notification {
        return notificationRepository.findById(notificationId).orElseThrow()
    }

    fun findAllByUserId(userId: Long): List<Notification> {
        return notificationRepository.findAllByUserId(userId)
    }
}