package com.newket.application.admin

import com.newket.application.admin.dto.AddTicketArtistsRequest
import com.newket.application.admin.dto.ArtistTableDto
import com.newket.application.admin.dto.PlaceTableDto
import com.newket.application.admin.dto.TicketTableResponse
import com.newket.application.artist.dto.common.ArtistDto
import com.newket.client.crawling.CreateTicketRequest
import com.newket.client.crawling.TicketCrawlingClient
import com.newket.client.gemini.TicketGeminiClient
import com.newket.core.util.DateUtil
import com.newket.domain.artist.ArtistReader
import com.newket.domain.artist.exception.ArtistAppender
import com.newket.domain.artist.exception.ArtistRemover
import com.newket.domain.ticket.*
import com.newket.domain.ticket_buffer.TicketBufferAppender
import com.newket.domain.ticket_buffer.TicketBufferReader
import com.newket.domain.ticket_buffer.TicketBufferRemover
import com.newket.domain.ticket_cache.TicketCacheAppender
import com.newket.domain.ticket_cache.TicketCacheRemover
import com.newket.infra.jpa.ticket.entity.*
import com.newket.infra.jpa.ticket_artist.entity.LineupImage
import com.newket.infra.jpa.ticket_artist.entity.TicketArtist
import com.newket.infra.mongodb.ticket_buffer.entity.TicketArtistBuffer
import com.newket.infra.mongodb.ticket_buffer.entity.TicketBuffer
import com.newket.infra.mongodb.ticket_buffer.entity.TicketSaleBuffer
import com.newket.infra.mongodb.ticket_cache.entity.Artist
import com.newket.infra.mongodb.ticket_cache.entity.TicketCache
import com.newket.infra.mongodb.ticket_cache.entity.TicketEventSchedule
import com.newket.infra.mongodb.ticket_cache.entity.TicketSaleSchedule
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class AdminService(
    private val ticketCrawlingClient: TicketCrawlingClient,
    private val artistReader: ArtistReader,
    private val placeReader: PlaceReader,
    private val ticketGeminiClient: TicketGeminiClient,
    private val ticketAppender: TicketAppender,
    private val ticketBufferAppender: TicketBufferAppender,
    private val ticketBufferRemover: TicketBufferRemover,
    private val ticketRemover: TicketRemover,
    private val ticketCacheRemover: TicketCacheRemover,
    private val ticketReader: TicketReader,
    private val ticketCacheAppender: TicketCacheAppender,
    private val ticketBufferReader: TicketBufferReader,
    private val artistRemover: ArtistRemover,
    private val artistAppender: ArtistAppender,
    private val placeAppender: PlaceAppender,
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
        return ticketInfo.copy(
            artists = artists, place = place, ticketEventSchedule = ticketEventSchedules, price = price
        )
    }

    //아티스트 크롤링
    fun fetchTicketArtist(text: String): List<CreateTicketRequest.Artist> {
        val artistList =
            artistReader.findAll().map { "${it.id} ${it.name} ${it.subName ?: ""} ${it.nickname ?: ""} " }.toString()
        return ticketGeminiClient.getArtists(text, artistList)
    }

    // 아티스트 자동완성
    fun searchArtist(keyword: String): List<CreateTicketRequest.Artist> {
        return artistReader.autocompleteByKeyword(keyword).map { artist ->
            CreateTicketRequest.Artist(
                artistId = artist.id,
                name = "**${artist.name}** ${artist.subName ?: ""} ${artist.nickname ?: ""}",
            )
        }
    }

    //티켓 추가
    @Transactional
    fun createTicketBuffer(request: CreateTicketRequest) {
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
        val ticketSaleScheduleList = request.ticketSaleUrls.map {
            val ticketSaleUrl = ticketAppender.saveTicketSaleUrl(
                TicketSaleUrl(
                    ticket = ticket, ticketProvider = it.ticketProvider, url = it.url, isDirectUrl = it.isDirectUrl
                )
            )
            it.ticketSaleSchedules.map { schedule ->
                ticketAppender.saveTicketSaleSchedule(
                    TicketSaleSchedule(
                        ticketSaleUrl = ticketSaleUrl, day = schedule.day, time = schedule.time, type = schedule.type
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
        val ticketSaleSchedules =
            ticketSaleScheduleList.sortedBy { it.time }.sortedBy { it.day }.map { Triple(it.type, it.day, it.time) }
                .distinct()

        val ticketBuffer = TicketBuffer(ticketId = ticket.id,
            genre = ticket.genre,
            imageUrl = ticket.imageUrl,
            title = ticket.title,
            customDate = DateUtil.dateToString(eventSchedules.map { it.day }),
            ticketEventSchedules = eventSchedules.map {
                TicketEventSchedule(
                    dateTime = LocalDateTime.of(it.day, it.time),
                    customDateTime = DateUtil.dateTimeToString(it.day, it.time),
                )
            },
            ticketSaleSchedules = ticketSaleSchedules.map { ticketSaleSchedule ->
                TicketSaleSchedule(
                    type = ticketSaleSchedule.first,
                    dateTime = LocalDateTime.of(ticketSaleSchedule.second, ticketSaleSchedule.third),
                )
            },
            artists = ticketArtists.map { ticketArtist ->
                Artist(
                    artistId = ticketArtist.artist.id,
                    name = ticketArtist.artist.name,
                    subName = ticketArtist.artist.subName,
                    nickname = ticketArtist.artist.nickname,
                )
            })
        ticketBufferAppender.saveTicketBuffer(ticketBuffer)
    }

    @Transactional
    fun createTicketSaleBuffer(ticketSaleBuffer: TicketSaleBuffer) {
        ticketBufferAppender.saveTicketSaleBuffer(ticketSaleBuffer)
    }

    @Transactional
    fun createTicketArtistBuffer(request: AddTicketArtistsRequest) {
        request.artists.map {
            val ticketArtistBuffer = TicketArtistBuffer(ticketId = request.ticketId, artistId = it.artistId)
            ticketBufferAppender.saveTicketArtistBuffer(ticketArtistBuffer)
        }
    }

    fun getTicketBuffer(): List<TicketTableResponse> {
        return ticketBufferReader.findAllTicketBuffer().map { ticketBuffer ->
            val ticket = ticketReader.findTicketById(ticketBuffer.ticketId)
            val ticketId = ticketBuffer.ticketId
            val eventSchedules =
                ticketReader.findAllEventScheduleByTicketId(ticketId).sortedBy { it.time }.sortedBy { it.day }
            val ticketSaleSchedules =
                ticketReader.findAllTicketSaleScheduleByTicketId(ticketId).sortedBy { it.time }.sortedBy { it.day }
                    .groupBy { Triple(it.type, it.day, it.time) }
                    .mapValues { entry ->
                        entry.value.map { it.ticketSaleUrl }
                    }

            TicketTableResponse(
                ticketId = ticketId,
                title = ticket.title,
                place = ticket.place.placeName,
                date = DateUtil.dateToString(eventSchedules.map { it.day }.toList()),
                dateList = DateUtil.dateTimeToString(eventSchedules.map { Pair(it.day, it.time) }),
                ticketSaleSchedules = ticketSaleSchedules.map { (ticketSaleSchedule, ticketProvider) ->
                    TicketTableResponse.TicketSaleScheduleDto(
                        type = ticketSaleSchedule.first,
                        date = DateUtil.dateTimeToString(ticketSaleSchedule.second, ticketSaleSchedule.third),
                        ticketSaleUrls = ticketProvider.map {
                            TicketTableResponse.TicketSaleUrlDto(
                                ticketProvider = it.ticketProvider.providerName,
                                providerImageUrl = it.ticketProvider.imageUrl,
                                url = it.url
                            )
                        }
                    )
                },
                prices = ticketReader.findAllPricesByTicketId(ticketId).map {
                    TicketTableResponse.PriceDto(
                        type = it.type,
                        price = it.price
                    )
                },
                artists = artistReader.findAllByTicketId(ticketId).map {
                    ArtistDto(
                        artistId = it.id,
                        name = it.name,
                        subName = it.subName,
                        imageUrl = it.imageUrl
                    )
                },
            )
        }
    }

    @Transactional
    fun deleteTicketBuffer(ticketId: Long) {
        ticketBufferRemover.deleteByTicketId(ticketId)
        ticketRemover.deleteByTicketId(ticketId)
    }

    fun getAllArtist(): List<ArtistTableDto> {
        return artistReader.findAll().map {
            ArtistTableDto(
                artistId = it.id,
                name = it.name,
                subName = it.subName,
                nickname = it.nickname,
                imageUrl = it.imageUrl
            )
        }
    }

    @Transactional
    fun putAllArtists(artistList: List<ArtistTableDto>) {// 1. 기존 아티스트 조회
        val existingArtists = artistReader.findAll().associateBy { it.id }
        val incomingIds = artistList.mapNotNull { it.artistId.takeIf { id -> id != 0L } }.toSet()

        // 2. 수정 및 생성
        val artistsToSave = artistList.map { dto ->
            if (dto.artistId != 0L && existingArtists.containsKey(dto.artistId)) {
                // 수정
                existingArtists[dto.artistId]!!.apply {
                    name = dto.name
                    subName = dto.subName
                    nickname = dto.nickname
                    imageUrl = dto.imageUrl
                }
            } else {
                // 생성: 새 엔티티
                com.newket.infra.jpa.artist.entity.Artist(
                    name = dto.name,
                    subName = dto.subName,
                    nickname = dto.nickname,
                    imageUrl = dto.imageUrl
                )
            }
        }

        // 3. 삭제: artistList에 없는 기존 아티스트
        val artistsToDelete = existingArtists.filterKeys { !incomingIds.contains(it) }
        artistRemover.deleteAll(artistsToDelete.values.toList())

        // 4. 저장: 수정 및 신규 아티스트
        artistAppender.saveAll(artistsToSave)
    }

    fun getAllPlaces(): List<PlaceTableDto> {
        return placeReader.findAll().map {
            PlaceTableDto(
                id = it.id,
                placeName = it.placeName,
                url = it.url
            )
        }
    }

    @Transactional
    fun putAllPlaces(placeList: List<PlaceTableDto>) {
        val existingPlaces = placeReader.findAll().associateBy { it.id }
        val placesToSave = placeList.map { dto ->
            if (dto.id != 0L && existingPlaces.containsKey(dto.id)) {
                // 수정
                existingPlaces[dto.id]!!.apply {
                    placeName = dto.placeName
                    url = dto.url
                }
            } else {
                // 생성: 새 엔티티
                Place(
                    placeName = dto.placeName,
                    url = dto.url
                )
            }
        }
        placeAppender.saveAll(placesToSave)
    }

    //판매 중인 티켓 -> ticketCache
    fun saveTicketCache() {
        ticketCacheRemover.deleteAllTicketCache()
        val ticketEventSchedules = ticketReader.findAllSellingTicket().groupBy { it.ticket }

        val ticketCaches: List<TicketCache> = ticketEventSchedules.map { (ticket, eventSchedules) ->
            val ticketSaleSchedules =
                ticketReader.findAllTicketSaleScheduleByTicketId(ticket.id).sortedBy { it.time }.sortedBy { it.day }
                    .map { Triple(it.type, it.day, it.time) }.distinct()

            TicketCache(ticketId = ticket.id,
                genre = ticket.genre,
                imageUrl = ticket.imageUrl,
                title = ticket.title,
                customDate = DateUtil.dateToString(eventSchedules.map { it.day }),
                ticketEventSchedules = eventSchedules.map {
                    TicketEventSchedule(
                        dateTime = LocalDateTime.of(it.day, it.time),
                        customDateTime = DateUtil.dateTimeToString(it.day, it.time),
                    )
                },
                ticketSaleSchedules = ticketSaleSchedules.map { ticketSaleSchedule ->
                    TicketSaleSchedule(
                        type = ticketSaleSchedule.first,
                        dateTime = LocalDateTime.of(ticketSaleSchedule.second, ticketSaleSchedule.third)
                    )
                },
                artists = artistReader.findAllByTicketId(ticket.id).map {
                    Artist(
                        artistId = it.id,
                        name = it.name,
                        subName = it.subName,
                        nickname = it.nickname,
                    )
                })
        }
        ticketCacheAppender.saveAllTicketCache(ticketCaches)
    }
}