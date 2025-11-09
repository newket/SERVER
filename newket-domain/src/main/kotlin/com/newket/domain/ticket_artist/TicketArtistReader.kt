package com.newket.domain.ticket_artist

import com.newket.infra.jpa.ticket_artist.repository.TicketLineupRepository
import com.newket.infra.jpa.ticket_artist.repository.MusicalArtistRepository
import com.newket.infra.jpa.ticket_artist.repository.TicketArtistRepository
import org.springframework.stereotype.Service

@Service
class TicketArtistReader(
    private val ticketLineupRepository: TicketLineupRepository,
    private val musicalArtistRepository: MusicalArtistRepository,
    private val ticketArtistRepository: TicketArtistRepository,
) {
    fun findLineUpByTicketId(ticketId: Long) = ticketLineupRepository.findByTicketId(ticketId)

    fun findMusicalArtistByTicketArtistId(ticketArtistId: Long) =
        musicalArtistRepository.findByTicketArtistId(ticketArtistId)

    fun findTicketArtistByTicketId(ticketId: Long) = ticketArtistRepository.findAllByTicketId(ticketId)

    fun findAllTicketArtistsByTicketIds(ticketIds: List<Long>) =
        ticketArtistRepository.findAllTicketArtistsByTicketIdIn(ticketIds)
}