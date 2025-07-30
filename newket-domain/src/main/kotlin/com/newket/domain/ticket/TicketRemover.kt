package com.newket.domain.ticket

import com.newket.infra.jpa.ticket.repository.*
import com.newket.infra.jpa.ticket_artist.repository.MusicalArtistRepository
import com.newket.infra.jpa.ticket_artist.repository.TicketLineupRepository
import com.newket.infra.jpa.ticket_artist.repository.TicketArtistRepository
import org.springframework.stereotype.Service

@Service
class TicketRemover(
    private val ticketRepository: TicketRepository,
    private val ticketSaleScheduleRepository: TicketSaleScheduleRepository,
    private val ticketSaleUrlRepository: TicketSaleUrlRepository,
    private val ticketPriceRepository: TicketPriceRepository,
    private val ticketArtistRepository: TicketArtistRepository,
    private val ticketEventScheduleRepository: TicketEventScheduleRepository,
    private val ticketLineupRepository: TicketLineupRepository,
    private val musicalArtistRepository: MusicalArtistRepository
) {
    fun deleteByTicketId(ticketId: Long) {
        ticketSaleScheduleRepository.findAllByTicketId(ticketId).map {
            ticketSaleScheduleRepository.deleteById(it.id)
        }
        ticketLineupRepository.deleteByTicketId(ticketId)
        ticketSaleUrlRepository.deleteAllByTicketId(ticketId)
        ticketPriceRepository.deleteAllByTicketId(ticketId)
        ticketEventScheduleRepository.deleteAllByTicketId(ticketId)
        ticketArtistRepository.deleteAllByTicketId(ticketId).map {
            musicalArtistRepository.deleteByTicketArtistId(it.id)
        }
        ticketRepository.deleteById(ticketId)
    }

    fun deleteInfoByTicketId(ticketId: Long) {
        ticketSaleScheduleRepository.findAllByTicketId(ticketId).map {
            ticketSaleScheduleRepository.deleteById(it.id)
        }
        ticketSaleUrlRepository.deleteAllByTicketId(ticketId)
        ticketPriceRepository.deleteAllByTicketId(ticketId)
        ticketEventScheduleRepository.deleteAllByTicketId(ticketId)
        ticketArtistRepository.deleteAllByTicketId(ticketId)
    }
}