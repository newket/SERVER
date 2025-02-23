package com.newket.domain.notification_request

import com.newket.infra.jpa.notification_request.repository.ArtistNotificationRepository
import com.newket.infra.jpa.notification_request.repository.TicketNotificationRepository
import org.springframework.stereotype.Service

@Service
class NotificationRequestReader(
    private val artistNotificationRepository: ArtistNotificationRepository,
    private val ticketNotificationRepository: TicketNotificationRepository
) {
    fun findArtistNotificationOrNull(userId: Long, artistId: Long) =
        artistNotificationRepository.findByUserIdAndArtistId(userId, artistId)

    fun findTicketNotificationOrNull(ticketId: Long, userId: Long) =
        ticketNotificationRepository.findByTicketIdAndUserId(ticketId, userId)

    fun findAllArtistNotification(userId: Long) = artistNotificationRepository.findAllByUserId(userId)

    fun findAllTicketNotification(userId: Long) = ticketNotificationRepository.findAllByUserId(userId)

}