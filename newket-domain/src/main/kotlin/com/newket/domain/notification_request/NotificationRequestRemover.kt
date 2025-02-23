package com.newket.domain.notification_request

import com.newket.infra.jpa.notification_request.repository.ArtistNotificationRepository
import com.newket.infra.jpa.notification_request.repository.TicketNotificationRepository
import org.springframework.stereotype.Service

@Service
class NotificationRequestRemover(
    private val artistNotificationRepository: ArtistNotificationRepository,
    private val ticketNotificationRepository: TicketNotificationRepository
) {
    fun deleteArtistNotification(userId: Long, artistId: Long) {
        artistNotificationRepository.deleteByUserIdAndArtistId(userId, artistId)
    }

    fun deleteTicketNotification(ticketId: Long, userId: Long) =
        ticketNotificationRepository.deleteByTicketIdAndUserId(ticketId, userId)
}