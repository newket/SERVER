package com.newket.domain.ticket

import com.newket.infra.jpa.ticket.entity.*
import com.newket.infra.jpa.ticket.repository.*
import com.newket.infra.jpa.ticket_artist.entity.TicketArtist
import com.newket.infra.jpa.ticket_artist.entity.TicketLineup
import com.newket.infra.jpa.ticket_artist.repository.TicketArtistRepository
import com.newket.infra.jpa.ticket_artist.repository.TicketLineupRepository
import org.springframework.stereotype.Service

@Service
class TicketAppender(
    private val ticketRepository: TicketRepository,
    private val ticketEventScheduleRepository: TicketEventScheduleRepository,
    private val ticketSaleUrlRepository: TicketSaleUrlRepository,
    private val ticketSaleScheduleRepository: TicketSaleScheduleRepository,
    private val ticketArtistRepository: TicketArtistRepository,
    private val ticketPriceRepository: TicketPriceRepository,
    private val ticketLineupRepository: TicketLineupRepository,
) {
    fun saveTicket(ticket: Ticket) = ticketRepository.save(ticket)

    fun saveTicketEventSchedule(ticketEventSchedule: TicketEventSchedule) =
        ticketEventScheduleRepository.save(ticketEventSchedule)

    fun saveTicketSaleUrl(ticketSaleUrl: TicketSaleUrl) = ticketSaleUrlRepository.save(ticketSaleUrl)

    fun saveTicketSaleSchedule(ticketSaleSchedule: TicketSaleSchedule) =
        ticketSaleScheduleRepository.save(ticketSaleSchedule)

    fun saveTicketArtist(ticketArtist: TicketArtist) = ticketArtistRepository.save(ticketArtist)

    fun saveAllTicketArtist(ticketArtists: List<TicketArtist>) = ticketArtistRepository.saveAll(ticketArtists)

    fun saveLineupImage(lineupImage: TicketLineup) = ticketLineupRepository.save(lineupImage)

    fun saveTicketPrice(ticketPrice: TicketPrice) = ticketPriceRepository.save(ticketPrice)
}