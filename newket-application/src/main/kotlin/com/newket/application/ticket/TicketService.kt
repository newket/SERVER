package com.newket.application.ticket

import com.newket.application.artist.dto.common.ArtistDto
import com.newket.application.ticket.dto.*
import com.newket.client.crawling.CreateTicketRequest
import com.newket.client.crawling.TicketCrawlingClient
import com.newket.client.gemini.TicketGeminiClient
import com.newket.core.util.DateUtil
import com.newket.domain.artist.ArtistReader
import com.newket.domain.ticket.service.PlaceReader
import com.newket.domain.ticket.service.TicketAppender
import com.newket.domain.ticket.service.TicketReader
import com.newket.domain.ticket.service.TicketRemover
import com.newket.domain.ticket_artist.TicketArtistReader
import com.newket.domain.ticket_buffer.TicketBufferAppender
import com.newket.domain.ticket_buffer.TicketBufferRemover
import com.newket.domain.ticket_cache.TicketCacheAppender
import com.newket.domain.ticket_cache.TicketCacheReader
import com.newket.domain.ticket_cache.TicketCacheRemover
import com.newket.infra.jpa.ticket.constant.Genre
import com.newket.infra.jpa.ticket.entity.Ticket
import com.newket.infra.jpa.ticket.entity.TicketEventSchedule
import com.newket.infra.jpa.ticket.entity.TicketPrice
import com.newket.infra.jpa.ticket.entity.TicketSaleSchedule
import com.newket.infra.jpa.ticket.entity.TicketSaleUrl
import com.newket.infra.jpa.ticket_artist.entity.LineupImage
import com.newket.infra.jpa.ticket_artist.entity.TicketArtist
import com.newket.infra.mongodb.ticket_buffer.entity.TicketBuffer
import com.newket.infra.mongodb.ticket_cache.entity.*
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
    private val placeReader: PlaceReader,
    private val ticketArtistReader: TicketArtistReader,
    private val ticketAppender: TicketAppender,
    private val ticketBufferAppender: TicketBufferAppender,
    private val ticketCacheReader: TicketCacheReader,
    private val ticketCacheAppender: TicketCacheAppender,
    private val ticketCacheRemover: TicketCacheRemover,
    private val ticketBufferRemover: TicketBufferRemover,
    private val ticketRemover: TicketRemover,
    private val ticketCrawlingClient: TicketCrawlingClient,
    private val ticketGeminiClient: TicketGeminiClient,
) {
    //티켓 크롤링
    fun fetchTicket(url: String): CreateTicketRequest {
        val ticketInfo = ticketCrawlingClient.fetchTicketInfo(url)
        val ticketRaw = ticketCrawlingClient.fetchTicketRaw(url)
        val artistList =
            artistReader.findAll().map { "${it.id} ${it.name} ${it.subName ?: ""} ${it.nickname ?: ""} " }.toString()
        val placeList = placeReader.findAll().map { it.placeName }.toString()
        val artists = ticketGeminiClient.getArtists(ticketRaw, artistList)
        val place = ticketGeminiClient.getPlace(ticketRaw, placeList)
        val price = ticketGeminiClient.getPrices(ticketRaw)
        val ticketEventSchedules = ticketGeminiClient.getTicketEventSchedules(ticketRaw)
        return ticketInfo.copy(artists = artists, place = place, ticketEventSchedule = ticketEventSchedules, price = price)
    }

    //티켓 추가
    @Transactional
    fun createTicket(request: CreateTicketRequest) {
        //mysql
        val artists = request.artists.map {
            artistReader.findById(it.artistId)
        }
        val ticket = Ticket(
            place = placeReader.findByPlaceName(request.place!!),
            title = request.title,
            imageUrl = request.imageUrl,
            genre = request.genre
        )
        ticketAppender.saveTicket(ticket)
        val ticketArtists = artists.map { artist ->
            TicketArtist(artist = artist, ticket = ticket)
        }
        ticketAppender.saveAllTicketArtist(ticketArtists)
        val eventSchedules = request.ticketEventSchedule.map {
            ticketAppender.saveTicketEventSchedule(
                TicketEventSchedule(ticket = ticket, day = it.day, time = it.time)
            )
        }.sortedBy { it.time }.sortedBy { it.day }
        val ticketSaleUrls = request.ticketSaleUrls.map {
            val ticketSaleUrl = ticketAppender.saveTicketSaleUrl(
                TicketSaleUrl(
                    ticket = ticket,
                    ticketProvider = it.ticketProvider,
                    url = it.url,
                    isDirectUrl = it.isDirectUrl
                )
            )
            it.ticketSaleSchedules.map { schedule ->
                ticketAppender.saveTicketSaleSchedule(
                    TicketSaleSchedule(
                        ticketSaleUrl = ticketSaleUrl,
                        day = schedule.day,
                        time = schedule.time,
                        type = schedule.type
                    )
                )
            }
        }.flatten()
        request.lineupImage?.let {
            ticketAppender.saveLineupImage(LineupImage(ticket = ticket, imageUrl = it))
        }
        request.price.map {
            ticketAppender.saveTicketPrice(TicketPrice(ticket = ticket, type = it.type, price = it.price))
        }


        //mongo
        val ticketSaleSchedules = ticketSaleUrls.sortedBy { it.time }.sortedBy { it.day }
            .groupBy { Triple(it.type, it.day, it.time) }
            .mapValues { entry ->
                entry.value.map { it.ticketSaleUrl }
            }
        val ticketBuffer = TicketBuffer(
            ticketId = ticket.id,
            genre = ticket.genre,
            imageUrl = ticket.imageUrl,
            title = ticket.title,
            place = ticket.place.placeName,
            placeUrl = ticket.place.url,
            customDate = DateUtil.dateToString(eventSchedules.map { it.day }),
            ticketEventSchedules = eventSchedules.map {
                TicketEventSchedule(
                    dateTime = LocalDateTime.of(it.day, it.time),
                    customDateTime = DateUtil.dateTimeToString(it.day, it.time),
                )
            },
            ticketSaleSchedules = ticketSaleSchedules.map { (ticketSaleSchedule, ticketSaleUrl) ->
                TicketSaleSchedule(
                    type = ticketSaleSchedule.first,
                    dateTime = LocalDateTime.of(ticketSaleSchedule.second, ticketSaleSchedule.third),
                    customDateTime = DateUtil.dateTimeToString(ticketSaleSchedule.second, ticketSaleSchedule.third),
                    ticketSaleUrls = ticketSaleUrl.map {
                        TicketSaleUrl(ticketProvider = it.ticketProvider, url = it.url)
                    },
                )
            },
            prices = request.price.map {
                TicketPrice(type = it.type, price = it.price)
            },
            lineupImage = request.lineupImage?.let {
                LineupImage(
                    message = when (ticket.genre) {
                        Genre.MUSICAL -> "캐스팅 일정 조회"
                        Genre.FESTIVAL -> "일자별 라인업 조회"
                        else -> "라인업 조회"
                    },
                    imageUrl = it,
                )
            },
            artists = ticketArtists.map { ticketArtist ->
                Artist(
                    artistId = ticketArtist.artist.id,
                    name = ticketArtist.artist.name,
                    subName = ticketArtist.artist.subName,
                    nickname = ticketArtist.artist.nickname,
                    role = null,
                    imageUrl = ticketArtist.artist.imageUrl
                )
            }
        )
        ticketBufferAppender.saveTicketBuffer(ticketBuffer)
    }

    @Transactional
    fun deleteTicketBuffer(ticketId: Long) {
        ticketBufferRemover.deleteByTicketId(ticketId)
        ticketRemover.deleteByTicketId(ticketId)
    }


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

    //판매 중인 티켓 -> ticketCache
    fun saveTicketCache() {
        ticketCacheRemover.deleteAllTicketCache()
        val ticketEventSchedules = ticketReader.findAllSellingTicket().groupBy { it.ticket }

        val ticketCaches: List<TicketCache> = ticketEventSchedules.map { (ticket, eventSchedules) ->
            val ticketSaleSchedules =
                ticketReader.findAllTicketSaleScheduleByTicketId(ticket.id).sortedBy { it.time }.sortedBy { it.day }
                    .groupBy { Triple(it.type, it.day, it.time) }
                    .mapValues { entry ->
                        entry.value.map { it.ticketSaleUrl }
                    }
            TicketCache(
                ticketId = ticket.id,
                genre = ticket.genre,
                imageUrl = ticket.imageUrl,
                title = ticket.title,
                place = ticket.place.placeName,
                placeUrl = ticket.place.url,
                customDate = DateUtil.dateToString(eventSchedules.map { it.day }),
                ticketEventSchedules = eventSchedules.map {
                    TicketEventSchedule(
                        dateTime = LocalDateTime.of(it.day, it.time),
                        customDateTime = DateUtil.dateTimeToString(it.day, it.time),
                    )
                },
                ticketSaleSchedules = ticketSaleSchedules.map { (ticketSaleSchedule, ticketSaleUrl) ->
                    TicketSaleSchedule(
                        type = ticketSaleSchedule.first,
                        dateTime = LocalDateTime.of(ticketSaleSchedule.second, ticketSaleSchedule.third),
                        customDateTime = DateUtil.dateTimeToString(ticketSaleSchedule.second, ticketSaleSchedule.third),
                        ticketSaleUrls = ticketSaleUrl.map {
                            TicketSaleUrl(
                                ticketProvider = it.ticketProvider,
                                url = it.url,
                            )
                        },

                        )
                },
                prices = ticketReader.findAllPricesByTicketId(ticket.id).map {
                    TicketPrice(
                        type = it.type,
                        price = it.price,
                    )
                },
                lineupImage = ticketArtistReader.findLineUpImageByTicketId(ticket.id)?.let {
                    LineupImage(
                        message = when (ticket.genre) {
                            Genre.MUSICAL -> "캐스팅 일정 조회"
                            Genre.FESTIVAL -> "일자별 라인업 조회"
                            else -> "라인업 조회"
                        },
                        imageUrl = it.imageUrl,
                    )
                },
                artists = artistReader.findAllByTicketId(ticket.id).map {
                    Artist(
                        artistId = it.id,
                        name = it.name,
                        subName = it.subName,
                        nickname = it.nickname,
                        imageUrl = it.imageUrl,
                        role = null,
                    )
                }
            )
        }
        ticketCacheAppender.saveAllTicketCache(ticketCaches)
    }
}