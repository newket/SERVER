package com.newket.scheduler.batch

import com.newket.domain.artist.ArtistReader
import com.newket.domain.ticket.TicketAppender
import com.newket.domain.ticket.TicketReader
import com.newket.domain.ticket_artist.TicketArtistReader
import com.newket.domain.ticket_buffer.TicketBufferReader
import com.newket.domain.ticket_buffer.TicketBufferRemover
import com.newket.domain.ticket_cache.TicketCacheAppender
import com.newket.domain.ticket_cache.TicketCacheModifier
import com.newket.domain.ticket_cache.TicketCacheRemover
import com.newket.infra.jpa.ticket.entity.TicketSaleSchedule
import com.newket.infra.jpa.ticket_artist.entity.TicketArtist
import com.newket.infra.mongodb.ticket_cache.entity.Artist
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
    private val ticketAppender: TicketAppender,
    private val ticketReader: TicketReader,
    private val ticketCacheModifier: TicketCacheModifier,
    private val artistReader: ArtistReader,
) {
    @Transactional
    fun createTicketCacheAndSendArtistNotification() {
        val ticketBuffers = ticketBufferReader.findAllTicketBuffer()

        // save
        val ticketCaches = ticketBuffers.map { ticket ->
            TicketCache(
                ticketId = ticket.ticketId,
                genre = ticket.genre,
                imageUrl = ticket.imageUrl,
                title = ticket.title,
                customDate = ticket.customDate,
                ticketEventSchedules = ticket.ticketEventSchedules,
                ticketSaleSchedules = ticket.ticketSaleSchedules,
                artists = ticket.artists
            )
        }
        ticketCacheAppender.saveAllTicketCache(ticketCaches)

        // sendFcm
        ticketBuffers.forEach { ticketBuffer ->
            val ticketArtists = ticketArtistReader.findTicketArtistByTicketId(ticketBuffer.ticketId)
            notificationManager.sendTicketNotification(ticketArtists)
        }

        // deleteAllBuffer
        ticketBufferRemover.deleteAllTicketBuffer()
    }

    @Transactional
    fun updateTicketSaleAndSendArtistNotification() {
        val ticketSaleBuffers = ticketBufferReader.findAllTicketSaleBuffer()

        ticketSaleBuffers.forEach {
            val ticketSaleUrl = ticketReader.findTicketSaleUrlById(it.ticketSaleUrlId)

            // mySql
            ticketAppender.saveTicketSaleSchedule(
                TicketSaleSchedule(
                    ticketSaleUrl = ticketSaleUrl,
                    day = it.dateTime.toLocalDate(), time = it.dateTime.toLocalTime(), type = it.type
                )
            )

            //mongo
            ticketCacheModifier.updateTicketSaleSchedule(
                it.ticketId, com.newket.infra.mongodb.ticket_cache.entity.TicketSaleSchedule(
                    type = it.type,
                    dateTime = it.dateTime
                )
            )
        }
        ticketSaleBuffers.map { it.ticketId }.distinct().forEach {
            val ticketArtists = ticketArtistReader.findTicketArtistByTicketId(it)
            notificationManager.sendTicketNotification(ticketArtists)
        }

        // deleteAllBuffer
        ticketBufferRemover.deleteAllTicketSaleBuffer()
    }

    @Transactional
    fun updateTicketArtistAndSendArtistNotification() {
        val ticketArtistBuffers = ticketBufferReader.findAllTicketArtistBuffer()

        val ticketArtists = ticketArtistBuffers.map {
            val ticket = ticketReader.findTicketById(it.ticketId)
            val artist = artistReader.findById(it.artistId)
            val ticketArtist = TicketArtist(artist, ticket)

            // mySql
            ticketAppender.saveTicketArtist(ticketArtist)

            //mongo
            ticketCacheModifier.updateTicketArtist(
                it.ticketId, Artist(artist.id, artist.name, artist.subName, artist.nickname)
            )
            ticketArtist
        }
        notificationManager.sendTicketNotification(ticketArtists)

        // deleteAllBuffer
        ticketBufferRemover.deleteAllTicketArtistBuffer()
    }

    @Transactional
    fun deleteAllAfterSaleTicketCache() {
        ticketCacheRemover.deleteAllAfterSaleTicketCache()
    }
}