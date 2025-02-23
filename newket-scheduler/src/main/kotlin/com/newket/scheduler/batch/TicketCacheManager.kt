package com.newket.scheduler.batch

import com.newket.domain.ticket_artist.TicketArtistReader
import com.newket.domain.ticket_buffer.TicketBufferReader
import com.newket.domain.ticket_buffer.TicketBufferRemover
import com.newket.domain.ticket_cache.TicketCacheAppender
import com.newket.domain.ticket_cache.TicketCacheRemover
import com.newket.infra.mongodb.ticket_cache.entity.TicketCache
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class TicketCacheManager(
    private val notificationManager: NotificationManager,
    private val ticketArtistReader: TicketArtistReader,
    private val ticketCacheAppender: TicketCacheAppender,
    private val ticketBufferReader: TicketBufferReader,
    private val ticketBufferRemover: TicketBufferRemover,
    private val ticketCacheRemover: TicketCacheRemover,
) {
    @Transactional
    fun saveTicketCacheAndSendArtistNotification() {
        val ticketBuffers = ticketBufferReader.findAllTicketBuffer()

        // save
        val ticketCaches = ticketBuffers.map { ticket ->
            TicketCache(
                ticketId = ticket.ticketId,
                genre = ticket.genre,
                imageUrl = ticket.imageUrl,
                title = ticket.title,
                place = ticket.place,
                placeUrl = ticket.placeUrl,
                customDate = ticket.customDate,
                ticketEventSchedules = ticket.ticketEventSchedules,
                ticketSaleSchedules = ticket.ticketSaleSchedules,
                prices = ticket.prices,
                lineupImage = ticket.lineupImage,
                artists = ticket.artists
            )
        }
        ticketCacheAppender.saveAllTicketCache(ticketCaches)

        // sendFcm
        ticketBuffers.map { ticketBuffer ->
            val ticketArtists = ticketArtistReader.findTicketArtistByTicketId(ticketBuffer.ticketId)
            notificationManager.sendFavoriteTicketOpeningNotice(ticketArtists)
        }

        // deleteAllBuffer
        ticketBufferRemover.deleteAllTicketBuffer()
    }

    @Transactional
    fun deleteAllOldTicketCache() {
        ticketCacheRemover.deleteAllOldTicketCache()
    }
}