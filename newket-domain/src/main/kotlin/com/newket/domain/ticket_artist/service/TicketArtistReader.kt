package com.newket.domain.ticket_artist.service

import com.newket.infra.jpa.ticket_artist.repository.LineupImageRepository
import com.newket.infra.jpa.ticket_artist.repository.MusicalArtistRepository
import com.newket.infra.jpa.ticket_artist.repository.TicketArtistRepository
import org.springframework.stereotype.Service

@Service
class TicketArtistReader(
    private val lineupImageRepository: LineupImageRepository,
    private val musicalArtistRepository: MusicalArtistRepository,
    private val ticketArtistRepository: TicketArtistRepository,
) {
    fun findLineUpImageByTicketId(ticketId: Long) = lineupImageRepository.findByTicketId(ticketId)

    fun findMusicalArtistByTicketArtistId(ticketArtistId: Long) =
        musicalArtistRepository.findByTicketArtistId(ticketArtistId)

    fun findTicketArtistByTicketId(ticketId: Long) = ticketArtistRepository.findAllByTicketId(ticketId)
}