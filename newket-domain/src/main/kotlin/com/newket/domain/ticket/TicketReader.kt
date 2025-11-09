package com.newket.domain.ticket

import com.newket.domain.ticket.exception.TicketException
import com.newket.infra.jpa.ticket.constant.Genre
import com.newket.infra.jpa.ticket.entity.*
import com.newket.infra.jpa.ticket.repository.*
import com.newket.infra.jpa.ticket_artist.repository.TicketArtistRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalTime

@Service
class TicketReader(
    private val ticketRepository: TicketRepository,
    private val ticketingRepository: TicketSaleScheduleRepository,
    private val scheduleRepository: TicketEventScheduleRepository,
    private val ticketArtistRepository: TicketArtistRepository,
    private val ticketPriceRepository: TicketPriceRepository,
    private val ticketEventScheduleRepository: TicketEventScheduleRepository,
    private val ticketSaleUrlRepository: TicketSaleUrlRepository,
    private val ticketSaleScheduleRepository: TicketSaleScheduleRepository,
) {
    fun findTicketById(ticketId: Long): Ticket {
        return ticketRepository.findById(ticketId).orElseThrow {
            TicketException.TicketNotFoundException()
        }
    }

    fun findAllEventScheduleByTicketId(id: Long): List<TicketEventSchedule> {
        return scheduleRepository.findAllByTicketId(id)
    }

    fun findAllTicketSaleScheduleByTicketId(ticketId: Long): List<TicketSaleSchedule> {
        return ticketingRepository.findAllByTicketId(ticketId)
    }

    fun findAllPricesByTicketId(ticketId: Long) = ticketPriceRepository.findAllByTicketId(ticketId)

    fun findTicketSaleUrlById(ticketSaleUrlId: Long): TicketSaleUrl =
        ticketSaleUrlRepository.findByIdOrNull(ticketSaleUrlId) ?: throw TicketException.TicketNotFoundException()

    fun findAllEventSchedulesByTicketIds(ticketIds: List<Long>): List<TicketEventSchedule> =
        ticketEventScheduleRepository.findAllByTicketIdIn(ticketIds)

    fun findAllTicketSaleSchedulesByTicketIds(ticketIds: List<Long>): List<TicketSaleSchedule> =
        ticketSaleScheduleRepository.findAllByTicketSaleUrlTicketIdIn(ticketIds)

    fun findAllPricesByTicketIds(ticketIds: List<Long>): List<TicketPrice> =
        ticketPriceRepository.findAllByTicketIdIn(ticketIds)

    // 판매완료
    fun findAllAfterSaleByArtistId(aristId: Long) =
        ticketArtistRepository.findAllAfterSaleByArtistId(aristId, LocalDate.now())

    fun findAllAfterSaleByArtistIdAndGenre(aristId: Long, genre: Genre): List<TicketEventSchedule> {
        if (genre == Genre.CONCERT_FESTIVAL) {
            val concerts =
                ticketArtistRepository.findAllAfterSaleByArtistIdAndGenre(aristId, Genre.CONCERT, LocalDate.now())
            val festivals =
                ticketArtistRepository.findAllAfterSaleByArtistIdAndGenre(aristId, Genre.FESTIVAL, LocalDate.now())
            return concerts + festivals
        }
        return ticketArtistRepository.findAllAfterSaleByArtistIdAndGenre(aristId, genre, LocalDate.now())
    }

    fun findAllAfterSaleTicketByGenre(genre: Genre): List<Ticket> =
        ticketRepository.findAllAfterSaleTicketByGenre(genre, LocalDate.now())

    // 오픈 1시간전, 하루 전 티켓
    fun findAllTicketSaleScheduleByDateAndTime(date: LocalDate, time: LocalTime): List<TicketSaleSchedule> {
        return ticketingRepository.findAllTicketSaleScheduleByDateAndTime(date, time)
    }
}