package com.newket.domain.notifiacation.service

import com.newket.infra.jpa.notifiacation.entity.Notification
import com.newket.infra.jpa.notifiacation.entity.TicketNotification
import com.newket.infra.jpa.notifiacation.repository.NotificationRepository
import com.newket.infra.jpa.notifiacation.repository.TicketNotificationRepository
import org.springframework.stereotype.Service

@Service
class NotificationReader(
    private val ticketNotificationRepository: TicketNotificationRepository,
    private val notificationRepository: NotificationRepository
) {
    //관심 공연
    fun findNotificationTicketOrNull(concertId: Long, userId: Long): TicketNotification? {
        return ticketNotificationRepository.findByTicketIdAndUserId(concertId, userId)
    }

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