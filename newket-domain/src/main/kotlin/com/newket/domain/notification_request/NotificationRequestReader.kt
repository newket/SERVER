package com.newket.domain.notification_request

import com.newket.infra.jpa.notification_request.repository.ArtistNotificationRepository
import com.newket.infra.jpa.notification_request.repository.TicketNotificationRepository
import com.newket.infra.mongodb.ticket_cache.entity.TicketCache
import com.newket.infra.mongodb.ticket_cache.repository.TicketCacheRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalTime

@Service
class NotificationRequestReader(
    private val artistNotificationRepository: ArtistNotificationRepository,
    private val ticketNotificationRepository: TicketNotificationRepository,
    private val ticketCacheRepository: TicketCacheRepository
) {
    fun findArtistNotificationOrNull(userId: Long, artistId: Long) =
        artistNotificationRepository.findByUserIdAndArtistId(userId, artistId)

    fun findTicketNotificationOrNull(ticketId: Long, userId: Long) =
        ticketNotificationRepository.findByTicketIdAndUserId(ticketId, userId)

    fun findAllArtistNotification(userId: Long) = artistNotificationRepository.findAllByUserId(userId)

    fun findAllTicketNotification(userId: Long) = ticketNotificationRepository.findAllByUserId(userId)

    fun findTop5BeforeSaleTickets(): List<TicketCache> {
        return ticketNotificationRepository.findTop5BeforeSaleTicketIds(LocalDate.now(), LocalTime.now()).map {
            ticketCacheRepository.findByTicketId(it)!!
        }
    }

    fun findTopNAfterSaleTickets(n: Int): List<TicketCache> {
        return ticketNotificationRepository.findTop5OnSaleTicketIds(
            LocalDate.now(),
            LocalTime.now(),
            PageRequest.of(0, n)
        ).map {
            ticketCacheRepository.findByTicketId(it)!!
        }
    }
}