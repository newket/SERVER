package com.newket.domain.ticket.service

import com.newket.domain.ticket.exception.TicketException
import com.newket.infra.jpa.ticket.entity.Ticket
import com.newket.infra.jpa.ticket.entity.TicketEventSchedule
import com.newket.infra.jpa.ticket.entity.TicketSaleSchedule
import com.newket.infra.jpa.ticket.repository.TicketEventScheduleRepository
import com.newket.infra.jpa.ticket.repository.TicketPriceRepository
import com.newket.infra.jpa.ticket.repository.TicketRepository
import com.newket.infra.jpa.ticket.repository.TicketSaleScheduleRepository
import com.newket.infra.jpa.ticket_artist.repository.TicketArtistRepository
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
) {
    fun findTicketById(ticketId: Long): Ticket {
        return ticketRepository.findById(ticketId).orElseThrow {
            TicketException.TicketNotFoundException()
        }
    }

    //오픈 예정 (티켓팅 날짜가 지금 이후 티켓)
    fun findAllBeforeOpenOrderByDay(): List<TicketSaleSchedule> {
        return ticketingRepository.findAllBeforeOpenOrderByDay(LocalDate.now(), LocalTime.now())
    }

    fun findAllBeforeOpenOrderById(): List<TicketSaleSchedule> {
        return ticketingRepository.findAllBeforeOpenOrderById(LocalDate.now(), LocalTime.now())
    }

    //예매 중 (티켓팅 날짜가 지금 이전, 공연이 지금 이후)
    fun findAllOnSaleOrderByDay(): List<TicketEventSchedule> {
        return ticketEventScheduleRepository.findAllOnSaleOrderByDay(LocalDate.now(), LocalTime.now())
    }

    fun findAllOnSaleOrderById(): List<TicketEventSchedule> {
        return ticketEventScheduleRepository.findAllOnSaleOrderById(LocalDate.now(), LocalTime.now())
    }

    //공연일정
    fun findAllEventScheduleByTicketId(id: Long): List<TicketEventSchedule> {
        return scheduleRepository.findAllByTicketId(id)
    }

    //티켓팅일정
    fun findAllTicketingScheduleByTicketId(ticketId: Long): List<TicketSaleSchedule> {
        return ticketingRepository.findALlByTicketId(ticketId)
    }

    //오픈 예정 티켓 검색 아티스트 또는 공연명
    fun findAllBeforeOpenContainsKeyword(keyword: String): List<TicketSaleSchedule> {
        return ticketingRepository.findAllOpeningNoticeContainsKeyword(LocalDate.now(), LocalTime.now(), keyword)
    }

    //예매 중 티켓 검색 아티스트 또는 공연명
    fun findAllOnSaleContainsKeyword(keyword: String): List<Ticket> {
        return ticketRepository.findAllOnSaleContainsKeyword(LocalDate.now(), LocalTime.now(), keyword)
    }

    //자동완성 검색
    fun autocompleteByKeyword(keyword: String): List<Ticket> {
        return ticketRepository.autocompleteByKeyword(keyword)
    }

    //관심 아티스트의 오픈예정 티켓
    fun findAllFavoriteArtistTicketOpen(userId: Long): List<TicketSaleSchedule> {
        return ticketingRepository.findAllFavoriteTicketByUserIdNowAfterOrderByIdDesc(
            userId,
            LocalDate.now(),
            LocalTime.now()
        )
    }

    //알림받기 신청한 티켓
    fun findAllTicketNotificationSaleSchedule(userId: Long): List<TicketSaleSchedule> {
        return ticketingRepository.findAllTicketNotificationSaleSchedule(
            userId,
            LocalDate.now(),
            LocalTime.now()
        )
    }

    // 오픈 1시간전 하루 전 티켓
    fun findAllTicketSaleScheduleByDateAndTime(date: LocalDate, time: LocalTime): List<TicketSaleSchedule> {
        return ticketingRepository.findAllTicketSaleScheduleByDateAndTime(date, time)
    }

    //아티스트 티켓
    fun findAllBeforeSaleByArtistId(aristId: Long) =
        ticketArtistRepository.findAllBeforeSaleByArtistId(aristId, LocalDate.now(), LocalTime.now())

    fun findAllOnSaleByArtistId(aristId: Long) =
        ticketArtistRepository.findAllOnSaleByArtistId(aristId, LocalDate.now(), LocalTime.now())

    fun findAllAfterSaleByArtistId(aristId: Long) =
        ticketArtistRepository.findAllAfterSaleByArtistId(aristId, LocalDate.now())

    //티켓 가격
    fun findAllPricesByTicketId(ticketId: Long) = ticketPriceRepository.findAllByTicketId(ticketId)

    //판매 중인 티켓
    fun findAllSellingTicket(): List<TicketEventSchedule> =
        ticketEventScheduleRepository.findAllSellingTicket(LocalDate.now())
}