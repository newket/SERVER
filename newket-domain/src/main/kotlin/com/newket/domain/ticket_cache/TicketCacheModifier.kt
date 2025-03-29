package com.newket.domain.ticket_cache

import com.newket.domain.ticket.exception.TicketException
import com.newket.infra.mongodb.ticket_cache.entity.Artist
import com.newket.infra.mongodb.ticket_cache.entity.TicketSaleSchedule
import com.newket.infra.mongodb.ticket_cache.repository.TicketCacheRepository
import org.springframework.stereotype.Service

@Service
class TicketCacheModifier(
    private val ticketCacheRepository: TicketCacheRepository
) {
    fun updateTicketSaleSchedule(ticketId: Long, newTicketSaleSchedule: TicketSaleSchedule) {
        val ticketCache =
            ticketCacheRepository.findByTicketId(ticketId) ?: throw TicketException.TicketNotFoundException()
        val updatedSchedules =
            (ticketCache.ticketSaleSchedules + newTicketSaleSchedule).distinctBy { Pair(it.type, it.dateTime) }
        ticketCache.updateTicketSaleSchedules(updatedSchedules)

        ticketCacheRepository.save(ticketCache)
    }

    fun updateTicketArtist(ticketId: Long, newTicketArtist: Artist) {
        val ticketCache =
            ticketCacheRepository.findByTicketId(ticketId) ?: throw TicketException.TicketNotFoundException()
        val updatedArtists =
            (ticketCache.artists + newTicketArtist).distinctBy { it.artistId }
        ticketCache.updateArtists(updatedArtists)
    }
}