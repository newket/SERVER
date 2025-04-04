package com.newket.application.admin

import com.newket.application.admin.dto.AddTicketArtistsRequest
import com.newket.client.crawling.CreateTicketRequest
import com.newket.client.crawling.TicketCrawlingClient
import com.newket.client.gemini.TicketGeminiClient
import com.newket.core.util.DateUtil
import com.newket.domain.artist.ArtistReader
import com.newket.domain.ticket.PlaceReader
import com.newket.domain.ticket.TicketAppender
import com.newket.domain.ticket.TicketReader
import com.newket.domain.ticket.TicketRemover
import com.newket.domain.ticket_buffer.TicketBufferAppender
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
    private val ticketCacheAppender: TicketCacheAppender
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

    @Transactional
    fun deleteTicketBuffer(ticketId: Long) {
        ticketBufferRemover.deleteByTicketId(ticketId)
        ticketRemover.deleteByTicketId(ticketId)
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