package com.newket.domain.ticket_cache

import com.newket.infra.mongodb.ticket_cache.repository.TicketCacheRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TicketCacheReader(
    private val ticketCacheRepository: TicketCacheRepository
) {
    //오픈 예정 티켓
    fun findAllBeforeSaleTicketOrderById() =
        ticketCacheRepository.findAllBeforeSaleTicket(LocalDateTime.now(), Sort.by(Sort.Order.desc("ticketId")))

    // 예매 중인 티켓 (최신 등록 순)
    fun findAllOnSaleTicketOrderById() =
        ticketCacheRepository.findAllOnSaleTicket(LocalDateTime.now(), Sort.by(Sort.Order.desc("ticketId")))

    // 예매 중인 티켓 (공연 날짜 임박순)
    fun findAllOnSaleTicketOrderByDay() = ticketCacheRepository.findAllOnSaleTicket(
        LocalDateTime.now(),
        Sort.by(Sort.Order.asc("ticketEventSchedules.dateTime"))
    )

    // 오픈 예정 티켓 검색
    fun findAllBeforeSaleTicketByKeyword(keyword: String) = ticketCacheRepository.findAllBeforeSaleTicketByKeyword(
        keyword,
        LocalDateTime.now(),
        PageRequest.of(0, 10, Sort.by(Sort.Order.asc("title")))
    )

    // 예매 중인 티켓 검색
    fun findAllOnSaleTicketByKeyword(keyword: String) = ticketCacheRepository.findAllOnSaleTicketByKeyword(
        keyword,
        LocalDateTime.now(),
        PageRequest.of(0, 10, Sort.by(Sort.Order.asc("title")))
    )

    // 티켓 검색 자동완성
    fun findAllTicketByKeyword(keyword: String) = ticketCacheRepository.findAllTicketByKeyword(
        keyword,
        PageRequest.of(0, 3, Sort.by(Sort.Order.asc("title")))
    )

    // 아티스트 오픈 예정 티켓
    fun findAllBeforeSaleTicketByArtistId(artistId: Long) =
        ticketCacheRepository.findAllBeforeSaleTicketByArtistId(
            artistId,
            LocalDateTime.now(),
            Sort.by(Sort.Order.asc("ticketEventSchedules.dateTime"))
        )

    // 아티스트 예매 중인 티켓
    fun findAllOnSaleTicketByArtistId(artistId: Long) = ticketCacheRepository.findAllOnSaleTicketByArtistId(
        artistId,
        LocalDateTime.now(),
        Sort.by(Sort.Order.asc("ticketEventSchedules.dateTime"))
    )

    // 오픈 예정 티켓 by artistIds
    fun findAllBeforeSaleTicketByArtistIds(artistIds: List<Long>) =
        ticketCacheRepository.findAllBeforeSaleTicketByArtistIds(
            artistIds, LocalDateTime.now()
        )

    // 예매 중인 티켓 by artistIds
    fun findAllOnSaleTicketByArtistIds(artistIds: List<Long>) = ticketCacheRepository.findAllOnSaleTicketByArtistIds(
        artistIds, LocalDateTime.now(), Sort.by(Sort.Order.asc("ticketEventSchedules.dateTime"))
    )

    // 오픈 예정 티켓 by TicketIds
    fun findAllBeforeSaleTicketByTicketIds(ticketIds: List<Long>) =
        ticketCacheRepository.findAllBeforeSaleTicketByTicketIds(
            ticketIds, LocalDateTime.now()
        )

}