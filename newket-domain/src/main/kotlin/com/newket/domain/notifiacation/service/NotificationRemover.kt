package com.newket.domain.notifiacation.service

import com.newket.infra.jpa.notifiacation.repository.TicketNotificationRepository
import org.springframework.stereotype.Service

@Service
class NotificationRemover(
    private val ticketNotificationRepository: TicketNotificationRepository
) {
    fun deleteNotificationTicketRequestByConcertIdAndUserId(concertId: Long, userId: Long) =
        ticketNotificationRepository.deleteByTicketIdAndUserId(concertId, userId)

    fun deleteAllNotificationTicketRequestByConcertIdInAndUserId(concertIds: List<Long>, userId: Long) =
        ticketNotificationRepository.deleteAllByTicketIdInAndUserId(concertIds, userId)
}