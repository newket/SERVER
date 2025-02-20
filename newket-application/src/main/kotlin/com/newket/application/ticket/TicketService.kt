package com.newket.application.ticket

import com.newket.application.ticket.dto.*
import com.newket.core.auth.getCurrentUserId
import com.newket.core.util.DateUtil
import com.newket.domain.artist.service.ArtistReader
import com.newket.domain.ticket.service.PlaceReader
import com.newket.domain.ticket.service.TicketAppender
import com.newket.domain.ticket.service.TicketReader
import com.newket.domain.ticket.service.TicketRemover
import com.newket.domain.ticket_artist.service.TicketArtistReader
import com.newket.domain.ticket_buffer.service.TicketBufferAppender
import com.newket.domain.ticket_buffer.service.TicketBufferRemover
import com.newket.domain.ticket_cache.service.TicketCacheAppender
import com.newket.domain.ticket_cache.service.TicketCacheReader
import com.newket.domain.ticket_cache.service.TicketCacheRemover
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
) {
    //티켓 추가
    @Transactional
    fun createTicket(request: CreateTicket.Request) {
        //mysql
        val artists = request.artist.map {
            artistReader.findByName(it)
        }
        val ticket = Ticket(
            place = placeReader.findByPlaceName(request.place),
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
    fun deleteTicketBuffer(ticketId: Long){
        ticketBufferRemover.deleteByTicketId(ticketId)
        ticketRemover.deleteByTicketId(ticketId)
    }


    //오픈 예정 티켓
    fun openingNotice(criteria: String): OpeningNotice.Response {
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

        return OpeningNotice.Response(
            totalNum = tickets.size,
            artistName = "",
            concerts = tickets.map { ticket ->
                OpeningNotice.Concert(
                    concertId = ticket.ticketId,
                    imageUrl = ticket.imageUrl,
                    title = ticket.title,
                    ticketingSchedules = ticket.ticketSaleSchedules.map {
                        OpeningNotice.ConcertTicketingSchedule(
                            type = it.type,
                            dDay = DateUtil.dateToDDay(it.dateTime.toLocalDate())
                        )
                    }
                )
            }
        )
    }

    //예매 중인 티켓
    fun onSale(criteria: String): OnSale.Response {
        val tickets = when (criteria) {
            "new" -> ticketCacheReader.findAllOnSaleTicketOrderById()
            else -> ticketCacheReader.findAllOnSaleTicketOrderByDay()
        }

        return OnSale.Response(
            totalNum = tickets.size,
            concerts = tickets.map { ticket ->
                OnSale.Concert(
                    concertId = ticket.ticketId,
                    imageUrl = ticket.imageUrl,
                    title = ticket.title,
                    date = ticket.customDate
                )
            }
        )
    }

    //티켓 상세 V2 // v2.0.4 이후 폐기
    fun ticketDetailV2(concertId: Long): TicketDetail.V2.Response {
        val concert = ticketReader.findConcertById(concertId)
        val schedules =
            ticketReader.findAllConcertScheduleByConcertId(concertId).sortedBy { it.time }.sortedBy { it.day }
        val ticketingSchedules =
            ticketReader.findAllTicketingScheduleByTicketId(concertId).sortedBy { it.time }.sortedBy { it.day }
                .groupBy { it.ticketSaleUrl }
        return TicketDetail.V2.Response(
            imageUrl = concert.imageUrl,
            title = concert.title,
            place = concert.place.placeName,
            placeUrl = concert.place.url,
            date = DateUtil.dateTimeToString(schedules.map { Pair(it.day, it.time) }),
            ticketProviders = ticketingSchedules.map { ticketProvider ->
                TicketDetail.V2.ConcertTicketProvider(
                    ticketProvider = ticketProvider.key.ticketProvider,
                    url = ticketProvider.key.url,
                    ticketingSchedules = ticketProvider.value.map { schedule ->
                        TicketDetail.V2.ConcertTicketingSchedule(
                            type = schedule.type,
                            date = DateUtil.dateToString(schedule.day),
                            time = TicketDetail().timeToString(schedule.time),
                            dDay = DateUtil.dateToDDay(schedule.day)
                        )
                    },
                )
            },
            isAvailableNotification = ticketingSchedules.any { ticketProvider ->
                ticketProvider.value.any { ticketingSchedules ->
                    (ticketingSchedules.day == LocalDate.now() && ticketingSchedules.time > LocalTime.now()) || ticketingSchedules.day > LocalDate.now()
                }
            },
            artists = artistReader.findAllByTicketId(concertId).map {
                TicketDetail.V2.Artist(
                    artistId = it.id,
                    name = it.name,
                    nicknames = it.subName
                )
            }
        )
    }

    // 티켓 상세
    fun ticketDetail(concertId: Long): TicketDetail.V1.Response {
        val concert = ticketReader.findConcertById(concertId)
        val schedules =
            ticketReader.findAllConcertScheduleByConcertId(concertId).sortedBy { it.time }.sortedBy { it.day }
        val ticketingSchedules =
            ticketReader.findAllTicketingScheduleByTicketId(concertId).sortedBy { it.time }.sortedBy { it.day }
                .groupBy { Triple(it.type, it.day, it.time) }
                .mapValues { entry ->
                    entry.value.map { it.ticketSaleUrl }
                }

        return TicketDetail.V1.Response(
            imageUrl = concert.imageUrl,
            title = concert.title,
            place = concert.place.placeName,
            placeUrl = concert.place.url,
            date = DateUtil.dateToString(schedules.map { it.day }.toList()),
            dateList = DateUtil.dateTimeToString(schedules.map { Pair(it.day, it.time) }),
            ticketingSchedules = ticketingSchedules.map { (ticketingSchedule, ticketProvider) ->
                TicketDetail.V1.ConcertTicketingSchedule(
                    type = ticketingSchedule.first,
                    date = DateUtil.dateTimeToString(ticketingSchedule.second, ticketingSchedule.third),
                    ticketProviders = ticketProvider.map {
                        TicketDetail.V1.ConcertTicketProvider(
                            ticketProvider = it.ticketProvider,
                            url = it.url
                        )
                    }
                )
            },
            prices = ticketReader.findAllPricesByTicketId(concertId).map {
                TicketDetail.V1.Price(
                    type = it.type,
                    price = it.price
                )
            },
            lineup = ticketArtistReader.findLineUpImageByTicketId(concertId)?.let {
                TicketDetail.V1.LineupImage(
                    message = when (concert.genre) {
                        Genre.MUSICAL -> "캐스팅 일정 조회"
                        Genre.FESTIVAL -> "일자별 라인업 조회"
                        else -> "라인업 조회"
                    },
                    imageUrl = it.imageUrl,
                )
            },
            artists = artistReader.findAllByTicketId(concertId).map {
                TicketDetail.V1.Artist(
                    artistId = it.id,
                    name = it.name,
                    subName = ticketArtistReader.findMusicalArtistByTicketArtistId(it.id)?.run { role } ?: it.subName,
                    imageUrl = it.imageUrl
                )
            },
            isAvailableNotification = ticketingSchedules.keys.any {
                (it.second == LocalDate.now() && it.third > LocalTime.now()) || it.second > LocalDate.now()
            },
        )
    }


    // v2.0.4 이후 폐기
    fun searchArtistsAndTickets(keyword: String): Search.Response {
        val beforeSaleTickets = ticketCacheReader.findAllBeforeSaleTicketByKeyword(keyword).map {
            it.copy(ticketSaleSchedules = it.ticketSaleSchedules.filter { schedule ->
                schedule.dateTime.isAfter(LocalDateTime.now()) || schedule.dateTime.isEqual(LocalDateTime.now())
            })
        }
        val onSaleTickets = ticketCacheReader.findAllOnSaleTicketByKeyword(keyword)

        return Search.Response(
            artists = artistReader.searchByKeyword(keyword).map {
                Search.Artist(
                    name = it.name, nicknames = it.subName, artistId = it.id
                )
            },
            openingNotice = OpeningNotice.Response(
                totalNum = beforeSaleTickets.size,
                artistName = "",
                concerts = beforeSaleTickets.map { ticket ->
                    OpeningNotice.Concert(
                        concertId = ticket.ticketId,
                        imageUrl = ticket.imageUrl,
                        title = ticket.title,
                        ticketingSchedules = ticket.ticketSaleSchedules.map {
                            OpeningNotice.ConcertTicketingSchedule(
                                type = it.type,
                                dDay = DateUtil.dateToDDay(it.dateTime.toLocalDate())
                            )
                        }.toSet().toList()
                    )
                }
            ),
            onSale = OnSale.Response(
                totalNum = onSaleTickets.size,
                concerts = onSaleTickets.map { ticket ->
                    OnSale.Concert(
                        concertId = ticket.ticketId,
                        imageUrl = ticket.imageUrl,
                        title = ticket.title,
                        date = ticket.customDate
                    )
                }
            ),
        )
    }

    // 검색 결과
    fun searchResult(keyword: String): SearchResult.Response {
        val beforeSaleTickets = ticketCacheReader.findAllBeforeSaleTicketByKeyword(keyword).map {
            it.copy(ticketSaleSchedules = it.ticketSaleSchedules.filter { schedule ->
                schedule.dateTime.isAfter(LocalDateTime.now()) || schedule.dateTime.isEqual(LocalDateTime.now())
            })
        }
        val onSaleTickets = ticketCacheReader.findAllOnSaleTicketByKeyword(keyword)

        return SearchResult.Response(
            artists = artistReader.searchByKeyword(keyword).map {
                SearchResult.Artist(
                    artistId = it.id, name = it.name, subName = it.subName, imageUrl = it.imageUrl
                )
            },
            openingNotice = SearchResult.OpeningNotice.Response(
                totalNum = beforeSaleTickets.size,
                concerts = beforeSaleTickets.map { ticket ->
                    SearchResult.OpeningNotice.Concert(
                        concertId = ticket.ticketId,
                        imageUrl = ticket.imageUrl,
                        title = ticket.title,
                        ticketingSchedules = ticket.ticketSaleSchedules.map {
                            SearchResult.OpeningNotice.ConcertTicketingSchedule(
                                type = it.type,
                                dDay = DateUtil.dateToDDay(it.dateTime.toLocalDate())
                            )
                        }
                    )
                }
            ),
            onSale = OnSale.Response(
                totalNum = onSaleTickets.size,
                concerts = onSaleTickets.map { ticket ->
                    OnSale.Concert(
                        concertId = ticket.ticketId,
                        imageUrl = ticket.imageUrl,
                        title = ticket.title,
                        date = ticket.customDate
                    )
                }
            ),
        )
    }

    //자동완성
    fun autocomplete(keyword: String): Autocomplete.Response {
        return Autocomplete.Response(
            artists = artistReader.autocompleteByKeyword(keyword).map {
                Autocomplete.Artist(
                    artistId = it.id, name = it.name, subName = it.subName
                )
            },
            tickets = ticketCacheReader.findAllTicketByKeyword(keyword).map {
                Autocomplete.Ticket(
                    concertId = it.ticketId, title = it.title
                )
            }
        )
    }

    //관심 아티스트의 오픈 예정 티켓
    fun getFavoriteArtistOpeningNotices(): FavoriteArtistOpeningNotice.Response {
        val userId = getCurrentUserId()

        val concertList = ticketReader.findAllFavoriteArtistTicketOpen(userId)
        //관심 아티스트의 오픈 예정 티켓 없을 때
        if (concertList.isEmpty())
            return FavoriteArtistOpeningNotice.Response(
                totalNum = 0,
                //첫번째 아티스트 명 (첫번째 콘서트의 첫번째 아티스트)
                artistName = "NONE",
                favoriteArtistNames = artistReader.findAllFavoriteArtistsByUserId(userId).map {
                    artistReader.findById(it.artistId).orElseThrow().name
                },
                concerts = emptyList()
            )
        else { //관심 아티스트의 오픈 예정 티켓 있을 때
            val concerts = concertList.groupBy {
                it.ticketSaleUrl.ticket
            }
            return FavoriteArtistOpeningNotice.Response(
                totalNum = concerts.keys.size,
                //첫번째 아티스트 명 (첫번째 콘서트의 첫번째 아티스트)
                artistName = artistReader.findAllByTicketId(concerts.keys.first().id).first().name,
                favoriteArtistNames = artistReader.findAllFavoriteArtistsByUserId(userId).map {
                    artistReader.findById(it.artistId).orElseThrow().name
                },
                concerts = concerts.map { (concert, schedules) ->
                    FavoriteArtistOpeningNotice.Concert(
                        concertId = concert.id,
                        imageUrl = concert.imageUrl,
                        title = concert.title,
                        ticketingSchedules = schedules.map {
                            FavoriteArtistOpeningNotice.ConcertTicketingSchedule(
                                type = it.type,
                                dDay = DateUtil.dateToDDay(it.day)
                            )
                        }.toSet().toList()
                    )
                }
            )
        }
    }

    //판매 중인 티켓 -> ticketCache
    fun saveTicketCache() {
        ticketCacheRemover.deleteAllTicketCache()
        val ticketEventSchedules = ticketReader.findAllSellingTicket().groupBy { it.ticket }

        val ticketCaches: List<TicketCache> = ticketEventSchedules.map { (ticket, eventSchedules) ->
            val ticketSaleSchedules =
                ticketReader.findAllTicketingScheduleByTicketId(ticket.id).sortedBy { it.time }.sortedBy { it.day }
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