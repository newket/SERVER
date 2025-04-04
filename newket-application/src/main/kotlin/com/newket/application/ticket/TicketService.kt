package com.newket.application.ticket

import com.newket.application.artist.dto.common.ArtistDto
import com.newket.application.ticket.dto.*
import com.newket.core.util.DateUtil
import com.newket.domain.artist.ArtistReader
import com.newket.domain.ticket.TicketReader
import com.newket.domain.ticket_artist.TicketArtistReader
import com.newket.domain.ticket_cache.TicketCacheReader
import com.newket.infra.jpa.ticket.constant.Genre
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Service
@Transactional(readOnly = true)
class TicketService(
    private val ticketReader: TicketReader,
    private val artistReader: ArtistReader,
    private val ticketArtistReader: TicketArtistReader,
    private val ticketCacheReader: TicketCacheReader,
) {
    //오픈 예정 티켓
    fun getBeforeSaleTickets(criteria: String): BeforeSaleTicketsResponse {
        val tickets = ticketCacheReader.findAllBeforeSaleTicketOrderById().map {
            it.copy(ticketSaleSchedules = it.ticketSaleSchedules.filter { schedule ->
                schedule.dateTime.isAfter(LocalDateTime.now()) || schedule.dateTime.isEqual(LocalDateTime.now())
            })
        }.let {
            when (criteria) {
                "new" -> it
                else -> it.sortedBy { ticket -> ticket.ticketSaleSchedules.first().dateTime }
            }
        }

        return BeforeSaleTicketsResponse(
            totalNum = tickets.size,
            tickets = tickets.map { ticket ->
                BeforeSaleTicketsResponse.BeforeSaleTicketDto(
                    ticketId = ticket.ticketId,
                    imageUrl = ticket.imageUrl,
                    title = ticket.title,
                    ticketSaleSchedules = ticket.ticketSaleSchedules.map {
                        BeforeSaleTicketsResponse.TicketSaleScheduleDto(
                            type = it.type,
                            dDay = DateUtil.dateToDDay(it.dateTime.toLocalDate())
                        )
                    }
                )
            }
        )
    }

    //예매 중인 티켓
    fun getOnSaleTickets(criteria: String): OnSaleResponse {
        val tickets = when (criteria) {
            "new" -> ticketCacheReader.findAllOnSaleTicketOrderById()
            else -> ticketCacheReader.findAllOnSaleTicketOrderByDay()
        }

        return OnSaleResponse(
            totalNum = tickets.size,
            tickets = tickets.map { ticket ->
                OnSaleResponse.OnSaleTicketDto(
                    ticketId = ticket.ticketId,
                    imageUrl = ticket.imageUrl,
                    title = ticket.title,
                    date = ticket.customDate
                )
            }
        )
    }

    // 티켓 상세
    fun getTicketDetail(ticketId: Long): TicketDetailResponse {
        val ticket = ticketReader.findTicketById(ticketId)
        val eventSchedules =
            ticketReader.findAllEventScheduleByTicketId(ticketId).sortedBy { it.time }.sortedBy { it.day }
        val ticketSaleSchedules =
            ticketReader.findAllTicketSaleScheduleByTicketId(ticketId).sortedBy { it.time }.sortedBy { it.day }
                .groupBy { Triple(it.type, it.day, it.time) }
                .mapValues { entry ->
                    entry.value.map { it.ticketSaleUrl }
                }

        return TicketDetailResponse(
            imageUrl = ticket.imageUrl,
            title = ticket.title,
            place = ticket.place.placeName,
            placeUrl = ticket.place.url,
            date = DateUtil.dateToString(eventSchedules.map { it.day }.toList()),
            dateList = DateUtil.dateTimeToString(eventSchedules.map { Pair(it.day, it.time) }),
            ticketSaleSchedules = ticketSaleSchedules.map { (ticketSaleSchedule, ticketProvider) ->
                TicketDetailResponse.TicketSaleScheduleDto(
                    type = ticketSaleSchedule.first,
                    date = DateUtil.dateTimeToString(ticketSaleSchedule.second, ticketSaleSchedule.third),
                    ticketSaleUrls = ticketProvider.map {
                        TicketDetailResponse.TicketSaleUrlDto(
                            ticketProvider = it.ticketProvider.providerName,
                            providerImageUrl = it.ticketProvider.imageUrl,
                            url = it.url
                        )
                    }
                )
            },
            prices = ticketReader.findAllPricesByTicketId(ticketId).map {
                TicketDetailResponse.PriceDto(
                    type = it.type,
                    price = it.price
                )
            },
            lineup = ticketArtistReader.findLineUpImageByTicketId(ticketId)?.let {
                TicketDetailResponse.LineupImageDto(
                    message = when (ticket.genre) {
                        Genre.MUSICAL -> "캐스팅 일정 조회"
                        Genre.FESTIVAL -> "일자별 라인업 조회"
                        else -> "라인업 조회"
                    },
                    imageUrl = it.imageUrl,
                )
            },
            artists = artistReader.findAllByTicketId(ticketId).map {
                ArtistDto(
                    artistId = it.id,
                    name = it.name,
                    subName = ticketArtistReader.findMusicalArtistByTicketArtistId(it.id)?.run { role } ?: it.subName,
                    imageUrl = it.imageUrl
                )
            },
            isAvailableNotification = ticketSaleSchedules.keys.any {
                (it.second == LocalDate.now() && it.third > LocalTime.now()) || it.second > LocalDate.now()
            },
        )
    }

    // 공연명+아티스트로 검색
    fun searchResult(keyword: String): SearchResultResponse {
        val beforeSaleTickets = ticketCacheReader.findAllBeforeSaleTicketByKeyword(keyword).map {
            it.copy(ticketSaleSchedules = it.ticketSaleSchedules.filter { schedule ->
                schedule.dateTime.isAfter(LocalDateTime.now()) || schedule.dateTime.isEqual(LocalDateTime.now())
            })
        }
        val onSaleTickets = ticketCacheReader.findAllOnSaleTicketByKeyword(keyword)

        return SearchResultResponse(
            artists = artistReader.searchByKeyword(keyword).map {
                ArtistDto(
                    artistId = it.id, name = it.name, subName = it.subName, imageUrl = it.imageUrl
                )
            },
            beforeSaleTickets = BeforeSaleTicketsResponse(
                totalNum = beforeSaleTickets.size,
                tickets = beforeSaleTickets.map { ticket ->
                    BeforeSaleTicketsResponse.BeforeSaleTicketDto(
                        ticketId = ticket.ticketId,
                        imageUrl = ticket.imageUrl,
                        title = ticket.title,
                        ticketSaleSchedules = ticket.ticketSaleSchedules.map {
                            BeforeSaleTicketsResponse.TicketSaleScheduleDto(
                                type = it.type,
                                dDay = DateUtil.dateToDDay(it.dateTime.toLocalDate())
                            )
                        }
                    )
                }
            ),
            onSaleTickets = OnSaleResponse(
                totalNum = onSaleTickets.size,
                tickets = onSaleTickets.map { ticket ->
                    OnSaleResponse.OnSaleTicketDto(
                        ticketId = ticket.ticketId,
                        imageUrl = ticket.imageUrl,
                        title = ticket.title,
                        date = ticket.customDate
                    )
                }
            ),
        )
    }

    //자동완성
    fun autocomplete(keyword: String): AutocompleteResponse {
        return AutocompleteResponse(
            artists = artistReader.autocompleteByKeyword(keyword).map {
                AutocompleteResponse.Artist(
                    artistId = it.id, name = it.name, subName = it.subName
                )
            },
            tickets = ticketCacheReader.findAllTicketByKeyword(keyword).map {
                AutocompleteResponse.Ticket(
                    ticketId = it.ticketId, title = it.title
                )
            }
        )
    }
}