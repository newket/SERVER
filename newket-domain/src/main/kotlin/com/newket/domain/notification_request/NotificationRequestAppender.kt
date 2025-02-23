package com.newket.domain.notification_request

import com.newket.infra.jpa.notification_request.entity.ArtistNotification
import com.newket.infra.jpa.notification_request.entity.TicketNotification
import com.newket.infra.jpa.notification_request.repository.ArtistNotificationRepository
import com.newket.infra.jpa.notification_request.repository.TicketNotificationRepository
import org.springframework.stereotype.Service

@Service
class NotificationRequestAppender(
    private val artistNotificationRepository: ArtistNotificationRepository,
    private val ticketNotificationRepository: TicketNotificationRepository
) {
    fun saveArtistNotification(artistNotification: ArtistNotification) =
        artistNotificationRepository.save(artistNotification)

    fun saveTicketNotification(ticketNotification: TicketNotification) =
        ticketNotificationRepository.save(ticketNotification)
}