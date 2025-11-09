package com.newket.application.admin

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import com.newket.application.admin.dto.*
import com.newket.client.crawling.CreateMusicalRequest
import com.newket.client.crawling.CreateTicketRequest
import com.newket.client.crawling.TicketCrawlingClient
import com.newket.client.gemini.TicketGeminiClient
import com.newket.client.s3.S3Properties
import com.newket.core.util.DateUtil
import com.newket.domain.artist.ArtistReader
import com.newket.domain.artist.exception.ArtistAppender
import com.newket.domain.artist.exception.ArtistRemover
import com.newket.domain.ticket.*
import com.newket.domain.ticket_artist.TicketArtistAppender
import com.newket.domain.ticket_artist.TicketArtistReader
import com.newket.domain.ticket_buffer.TicketBufferAppender
import com.newket.domain.ticket_buffer.TicketBufferReader
import com.newket.domain.ticket_buffer.TicketBufferRemover
import com.newket.domain.ticket_cache.TicketCacheAppender
import com.newket.domain.ticket_cache.TicketCacheReader
import com.newket.domain.ticket_cache.TicketCacheRemover
import com.newket.infra.jpa.artist.entity.GroupMember
import com.newket.infra.jpa.ticket.constant.Genre
import com.newket.infra.jpa.ticket.entity.*
import com.newket.infra.jpa.ticket_artist.entity.MusicalArtist
import com.newket.infra.jpa.ticket_artist.entity.TicketArtist
import com.newket.infra.jpa.ticket_artist.entity.TicketLineup
import com.newket.infra.mongodb.ticket_buffer.entity.TicketArtistBuffer
import com.newket.infra.mongodb.ticket_buffer.entity.TicketBuffer
import com.newket.infra.mongodb.ticket_buffer.entity.TicketSaleBuffer
import com.newket.infra.mongodb.ticket_cache.entity.Artist
import com.newket.infra.mongodb.ticket_cache.entity.TicketCache
import com.newket.infra.mongodb.ticket_cache.entity.TicketEventSchedule
import com.newket.infra.mongodb.ticket_cache.entity.TicketSaleSchedule
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

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
    private val ticketArtistAppender: TicketArtistAppender,
    private val ticketCacheReader: TicketCacheReader,
    private val amazonS3Client: AmazonS3Client,
    private val s3Properties: S3Properties,
    private val ticketArtistReader: TicketArtistReader,
) {
    suspend fun fetchTicket(url: String): CreateTicketRequest = coroutineScope {
        val (ticketInfo, ticketRaw, artistList, placeList) = fetchTicketData(url)

        val artistsDeferred = async { ticketGeminiClient.getArtists(ticketRaw, artistList) }
        val placeDeferred = async { ticketGeminiClient.getPlace(ticketRaw, placeList) }
        val priceDeferred = async { ticketGeminiClient.getPrices(ticketRaw) }
        val ticketEventSchedulesDeferred = async { ticketGeminiClient.getTicketEventSchedules(ticketRaw) }

        ticketInfo.copy(
            artists = withTimeout(60 * 1000) { artistsDeferred.await() },
            place = withTimeout(60 * 1000) { placeDeferred.await() },
            ticketEventSchedule = withTimeout(60 * 1000) { ticketEventSchedulesDeferred.await() },
            price = withTimeout(60 * 1000) { priceDeferred.await() }
        )
    }

    suspend fun fetchMusical(url: String): CreateMusicalRequest = coroutineScope {
        val (ticketInfo, ticketRaw, artistList, placeList) = fetchTicketData(url)

        val artistsDeferred = async { ticketGeminiClient.getMusicalArtists(ticketRaw, artistList) }
        val placeDeferred = async { ticketGeminiClient.getPlace(ticketRaw, placeList) }
        val priceDeferred = async { ticketGeminiClient.getPrices(ticketRaw) }
        val ticketEventSchedulesDeferred = async { ticketGeminiClient.getTicketEventSchedules(ticketRaw) }

        CreateMusicalRequest(
            genre = Genre.MUSICAL,
            artists = withTimeout(60 * 1000) { artistsDeferred.await() },
            place = withTimeout(60 * 1000) { placeDeferred.await() },
            title = ticketInfo.title,
            imageUrl = ticketInfo.imageUrl,
            ticketEventSchedule = withTimeout(60 * 1000) { ticketEventSchedulesDeferred.await() },
            ticketSaleUrls = ticketInfo.ticketSaleUrls,
            lineupImage = ticketInfo.lineupImage,
            price = withTimeout(60 * 1000) { priceDeferred.await() }
        )
    }

    private suspend fun fetchTicketData(url: String): FetchTicketDataResult = coroutineScope {
        val ticketInfoDeferred = async { ticketCrawlingClient.fetchTicketInfo(url) }
        val ticketRawDeferred = async { ticketCrawlingClient.fetchTicketRaw(url) }
        val artistListDeferred = async {
            artistReader.findAll().map { "${it.id} ${it.name} ${it.subName ?: ""} ${it.nickname ?: ""}" }.toString()
        }
        val placeDeferred = async {
            placeReader.findAll().map { it.placeName }.toString()
        }

        val ticketInfo = withTimeout(2 * 60 * 1000) { ticketInfoDeferred.await() }
        val ticketRaw = withTimeout(2 * 60 * 1000) { ticketRawDeferred.await() }
        val artistList = artistListDeferred.await()
        val place = placeDeferred.await()

        FetchTicketDataResult(ticketInfo, ticketRaw, artistList, place)
    }

    private data class FetchTicketDataResult(
        val ticketInfo: CreateTicketRequest,
        val ticketRaw: String,
        val artistList: String,
        val placeList: String
    )

    //티켓 추가
    @Transactional
    fun createTicketBuffer(request: CreateTicketRequest) {
        val ticket = Ticket(
            place = placeReader.findByPlaceName(request.place!!),
            title = request.title,
            imageUrl = request.imageUrl,
            genre = request.genre
        )
        val savedTicket = saveTicket(ticket, request)
        saveTicketBuffer(ticket, savedTicket.eventSchedules, savedTicket.ticketSaleSchedules, savedTicket.ticketArtists)
    }

    @Transactional
    fun updateTicket(ticketId: Long, request: CreateTicketRequest) {
        val ticket = ticketReader.findTicketById(ticketId)

        // 기존 데이터 삭제
        ticketRemover.deleteInfoByTicketId(ticketId)

        // 티켓 기본 정보 업데이트
        ticket.apply {
            place = placeReader.findByPlaceName(request.place!!)
            title = request.title
            imageUrl = request.imageUrl
        }
        val savedTicket = saveTicket(ticket, request)

        // TicketCache에 해당 ticketId가 있는지 확인하고 있으면 삭제 후 추가
        val existingCache = ticketCacheReader.findByTicketId(ticket.id)
        if (existingCache != null) {
            ticketCacheRemover.deleteByTicketId(ticket.id)
            saveTicketCache(
                ticket,
                savedTicket.eventSchedules,
                savedTicket.ticketSaleSchedules,
                savedTicket.ticketArtists
            )
        }

        // TicketBuffer에 해당 ticketId가 있는지 확인하고 있으면 삭제 후 추가
        val existingBuffer = ticketBufferReader.findByTicketId(ticket.id)
        if (existingBuffer != null) {
            ticketBufferRemover.deleteByTicketId(ticket.id)
            saveTicketBuffer(
                ticket,
                savedTicket.eventSchedules,
                savedTicket.ticketSaleSchedules,
                savedTicket.ticketArtists
            )
        }
    }

    fun getTicket(ticketId: Long): CreateTicketRequest {
        val ticket = ticketReader.findTicketById(ticketId)
        val eventSchedules =
            ticketReader.findAllEventScheduleByTicketId(ticketId).sortedBy { it.time }.sortedBy { it.day }
        val ticketSaleSchedules =
            ticketReader.findAllTicketSaleScheduleByTicketId(ticketId).groupBy { it.ticketSaleUrl }

        return CreateTicketRequest(
            genre = ticket.genre,
            artists = ticketArtistReader.findTicketArtistByTicketId(ticketId).map {
                CreateTicketRequest.Artist(
                    artistId = it.artist.id,
                    name = it.artist.name,
                )
            },
            place = ticket.place.placeName,
            title = ticket.title,
            imageUrl = ticket.imageUrl,
            ticketEventSchedule = eventSchedules.map {
                CreateTicketRequest.TicketEventSchedule(
                    day = it.day,
                    time = it.time,
                )
            },
            ticketSaleUrls = ticketSaleSchedules.map {
                CreateTicketRequest.TicketSaleUrl(
                    ticketProvider = it.key.ticketProvider,
                    url = it.key.url,
                    isDirectUrl = false,
                    ticketSaleSchedules = it.value.map { schedule ->
                        CreateTicketRequest.TicketSaleSchedule(
                            day = schedule.day,
                            time = schedule.time,
                            type = schedule.type
                        )
                    }

                )
            },
            lineupImage = ticketArtistReader.findLineUpByTicketId(ticketId)?.imageUrl,
            price = ticketReader.findAllPricesByTicketId(ticketId)
                .map { CreateTicketRequest.Price(type = it.type, price = it.price) }
        )
    }

    @Transactional
    fun createMusical(request: CreateMusicalRequest) {
        val ticket = Ticket(
            place = placeReader.findByPlaceName(request.place!!),
            title = request.title,
            imageUrl = request.imageUrl,
            genre = Genre.MUSICAL
        )
        val savedTicket = saveMusical(ticket, request)
        saveTicketBuffer(ticket, savedTicket.eventSchedules, savedTicket.ticketSaleSchedules, savedTicket.ticketArtists)
    }

    @Transactional
    fun updateMusical(ticketId: Long, request: CreateMusicalRequest) {
        val ticket = ticketReader.findTicketById(ticketId)

        // 기존 데이터 삭제
        ticketRemover.deleteInfoByTicketId(ticketId)

        // 티켓 기본 정보 업데이트
        ticket.apply {
            place = placeReader.findByPlaceName(request.place!!)
            title = request.title
            imageUrl = request.imageUrl
        }
        val musicalTicket = saveMusical(ticket, request)

        // TicketCache에 해당 ticketId가 있는지 확인하고 있으면 삭제 후 추가
        val existingCache = ticketCacheReader.findByTicketId(ticket.id)
        if (existingCache != null) {
            ticketCacheRemover.deleteByTicketId(ticket.id)
            saveTicketCache(
                ticket,
                musicalTicket.eventSchedules,
                musicalTicket.ticketSaleSchedules,
                musicalTicket.ticketArtists
            )
        }

        // TicketBuffer에 해당 ticketId가 있는지 확인하고 있으면 삭제 후 추가
        val existingBuffer = ticketBufferReader.findByTicketId(ticket.id)
        if (existingBuffer != null) {
            ticketBufferRemover.deleteByTicketId(ticket.id)
            saveTicketBuffer(
                ticket,
                musicalTicket.eventSchedules,
                musicalTicket.ticketSaleSchedules,
                musicalTicket.ticketArtists
            )
        }
    }

    fun getMusical(ticketId: Long): CreateMusicalRequest {
        val ticket = ticketReader.findTicketById(ticketId)
        val eventSchedules =
            ticketReader.findAllEventScheduleByTicketId(ticketId).sortedBy { it.time }.sortedBy { it.day }
        val ticketSaleSchedules =
            ticketReader.findAllTicketSaleScheduleByTicketId(ticketId).groupBy { it.ticketSaleUrl }

        return CreateMusicalRequest(
            genre = ticket.genre,
            artists = ticketArtistReader.findTicketArtistByTicketId(ticketId).map {
                CreateMusicalRequest.Artist(
                    artistId = it.artist.id,
                    name = it.artist.name,
                    role = ticketArtistReader.findMusicalArtistByTicketArtistId(it.id)!!.role
                )
            },
            place = ticket.place.placeName,
            title = ticket.title,
            imageUrl = ticket.imageUrl,
            ticketEventSchedule = eventSchedules.map {
                CreateTicketRequest.TicketEventSchedule(
                    day = it.day,
                    time = it.time,
                )
            },
            ticketSaleUrls = ticketSaleSchedules.map {
                CreateTicketRequest.TicketSaleUrl(
                    ticketProvider = it.key.ticketProvider,
                    url = it.key.url,
                    isDirectUrl = false,
                    ticketSaleSchedules = it.value.map { schedule ->
                        CreateTicketRequest.TicketSaleSchedule(
                            day = schedule.day,
                            time = schedule.time,
                            type = schedule.type
                        )
                    }

                )
            },
            lineupImage = ticketArtistReader.findLineUpByTicketId(ticketId)?.imageUrl,
            price = ticketReader.findAllPricesByTicketId(ticketId)
                .map { CreateTicketRequest.Price(type = it.type, price = it.price) }
        )
    }

    private fun saveTicket(ticket: Ticket, request: CreateTicketRequest): TicketResponse {
        ticketAppender.saveTicket(ticket)
        val artists = request.artists.map {
            artistReader.findById(it.artistId)
        }
        val ticketArtists = artists.map { artist -> TicketArtist(artist = artist, ticket = ticket) }
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
            ticketAppender.saveLineupImage(TicketLineup(ticket = ticket, imageUrl = it))
        }
        request.price.map {
            ticketAppender.saveTicketPrice(TicketPrice(ticket = ticket, type = it.type, price = it.price))
        }

        val ticketSaleSchedules =
            ticketSaleScheduleList.sortedBy { it.time }.sortedBy { it.day }.map { Triple(it.type, it.day, it.time) }
                .distinct()
        return TicketResponse(eventSchedules, ticketSaleSchedules, ticketArtists)
    }

    private fun saveMusical(ticket: Ticket, request: CreateMusicalRequest): TicketResponse {
        ticketAppender.saveTicket(ticket)
        val artists = request.artists.map { artistReader.findById(it.artistId) }
        val ticketArtists = artists.map { artist -> TicketArtist(artist = artist, ticket = ticket) }
        val savedTicketArtists = ticketAppender.saveAllTicketArtist(ticketArtists)
        val musicalArtists = savedTicketArtists.mapIndexed { index, ticketArtist ->
            val requestArtist = request.artists[index]
            MusicalArtist(
                ticketArtistId = ticketArtist.id,
                role = requestArtist.role
            )
        }
        ticketArtistAppender.saveAllMusicalArtists(musicalArtists)

        if (!request.lineupImage.isNullOrEmpty()) {
            ticketAppender.saveLineupImage(TicketLineup(ticket = ticket, imageUrl = request.lineupImage!!))
        }

        request.price.map {
            ticketAppender.saveTicketPrice(TicketPrice(ticket = ticket, type = it.type, price = it.price))
        }

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

        val ticketSaleSchedules =
            ticketSaleScheduleList.sortedBy { it.time }.sortedBy { it.day }.map { Triple(it.type, it.day, it.time) }
                .distinct()

        return TicketResponse(eventSchedules, ticketSaleSchedules, ticketArtists)
    }

    private data class TicketResponse(
        val eventSchedules: List<com.newket.infra.jpa.ticket.entity.TicketEventSchedule>,
        val ticketSaleSchedules: List<Triple<String, LocalDate, LocalTime>>,
        val ticketArtists: List<TicketArtist>
    )


    private fun saveTicketBuffer(
        ticket: Ticket,
        eventSchedules: List<com.newket.infra.jpa.ticket.entity.TicketEventSchedule>,
        ticketSaleSchedules: List<Triple<String, LocalDate, LocalTime>>,
        ticketArtists: List<TicketArtist>
    ) {
        val ticketBuffer = TicketBuffer(
            ticketId = ticket.id,
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
            }
        )

        ticketBufferAppender.saveTicketBuffer(ticketBuffer)
    }

    private fun saveTicketCache(
        ticket: Ticket,
        eventSchedules: List<com.newket.infra.jpa.ticket.entity.TicketEventSchedule>,
        ticketSaleSchedules: List<Triple<String, LocalDate, LocalTime>>,
        ticketArtists: List<TicketArtist>,
    ) {
        val ticketCache = TicketCache(
            ticketId = ticket.id,
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
            }
        )

        ticketCacheAppender.saveTicketCache(ticketCache)
    }

    @Transactional
    fun createTicketSaleScheduleBuffer(request: AddTicketSaleScheduleRequest, ticketSaleUrlId: Long) {
        val ticketId = ticketReader.findTicketSaleUrlById(ticketSaleUrlId).ticket.id
        val ticketSaleBuffer = TicketSaleBuffer(
            ticketId = ticketId,
            ticketSaleUrlId = ticketSaleUrlId,
            dateTime = LocalDateTime.of(request.day, request.time),
            type = request.type
        )
        ticketBufferAppender.saveTicketSaleBuffer(ticketSaleBuffer)
    }

    @Transactional
    fun createTicketArtistBuffer(request: AddTicketArtistsRequest, ticketId: Long) {
        request.artists.map {
            val ticketArtistBuffer = TicketArtistBuffer(ticketId = ticketId, artistId = it.artistId)
            ticketBufferAppender.saveTicketArtistBuffer(ticketArtistBuffer)
        }
    }

    fun getTicketBuffer(genre: Genre): List<TicketTableResponse> {
        return ticketBufferReader.findAllTicketBufferByGenre(genre).map { ticketBuffer ->
            createTicketTableResponse(ticketBuffer.ticketId)
        }
    }

    fun getOnSaleTicket(genre: Genre): List<TicketTableResponse> {
        return ticketCacheReader.findAllByGenre(genre).map { onSaleTicket ->
            val ticket = ticketReader.findTicketById(onSaleTicket.ticketId)
            val ticketSaleSchedules =
                ticketReader.findAllTicketSaleScheduleByTicketId(onSaleTicket.ticketId).sortedBy { it.time }
                    .sortedBy { it.day }
                    .groupBy { Triple(it.type, it.day, it.time) }
                    .mapValues { entry -> entry.value.map { it.ticketSaleUrl } }

            TicketTableResponse(
                ticketId = onSaleTicket.ticketId,
                title = onSaleTicket.title,
                place = ticket.place.placeName,
                dateList = onSaleTicket.ticketEventSchedules.map {
                    DateUtil.dateTimeToString(it.dateTime.toLocalDate(), it.dateTime.toLocalTime())
                },
                ticketSaleSchedules = ticketSaleSchedules.map { (ticketSaleSchedule, ticketProvider) ->
                    TicketTableResponse.TicketSaleScheduleDto(
                        type = ticketSaleSchedule.first,
                        date = DateUtil.dateTimeToString(ticketSaleSchedule.second, ticketSaleSchedule.third),
                        ticketProviders = ticketProvider.map { it.ticketProvider.providerName }
                    )
                },
                prices = ticketReader.findAllPricesByTicketId(onSaleTicket.ticketId).map {
                    TicketTableResponse.PriceDto(
                        type = it.type,
                        price = it.price
                    )
                },
                artists = onSaleTicket.artists.map { it.name },
            )
        }
    }

    fun getAfterSaleTicket(genre: Genre): List<TicketTableResponse> {
        return ticketReader.findAllAfterSaleTicketByGenre(genre).map { afterSaleTicket ->
            createTicketTableResponse(afterSaleTicket.id)
        }
    }

    private fun createTicketTableResponse(ticketId: Long): TicketTableResponse {
        val ticket = ticketReader.findTicketById(ticketId)
        val eventSchedules =
            ticketReader.findAllEventScheduleByTicketId(ticketId).sortedBy { it.time }.sortedBy { it.day }
        val ticketSaleSchedules =
            ticketReader.findAllTicketSaleScheduleByTicketId(ticketId).sortedBy { it.time }.sortedBy { it.day }
                .groupBy { Triple(it.type, it.day, it.time) }
                .mapValues { entry -> entry.value.map { it.ticketSaleUrl } }

        return TicketTableResponse(
            ticketId = ticketId,
            title = ticket.title,
            place = ticket.place.placeName,
            dateList = DateUtil.dateTimeToString(eventSchedules.map { Pair(it.day, it.time) }),
            ticketSaleSchedules = ticketSaleSchedules.map { (ticketSaleSchedule, ticketProvider) ->
                TicketTableResponse.TicketSaleScheduleDto(
                    type = ticketSaleSchedule.first,
                    date = DateUtil.dateTimeToString(ticketSaleSchedule.second, ticketSaleSchedule.third),
                    ticketProviders = ticketProvider.map { it.ticketProvider.providerName }
                )
            },
            prices = ticketReader.findAllPricesByTicketId(ticketId).map {
                TicketTableResponse.PriceDto(
                    type = it.type,
                    price = it.price
                )
            },
            artists = artistReader.findAllTicketArtistsByTicketId(ticketId).map { it.artist.name },
        )
    }

    @Transactional
    fun deleteTicket(ticketId: Long) {
        ticketBufferRemover.deleteByTicketId(ticketId)
        ticketCacheRemover.deleteByTicketId(ticketId)
        ticketRemover.deleteByTicketId(ticketId)
    }

    // 아티스트DB
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
    fun putAllArtists(artistList: List<ArtistTableDto>) {
        val existingArtists = artistReader.findAll().associateBy { it.id }
        val incomingIds = artistList.mapNotNull { it.artistId.takeIf { id -> id != 0L } }.toSet()

        val artistsToSave = artistList.map { dto ->
            if (dto.artistId != 0L && existingArtists.containsKey(dto.artistId)) {
                existingArtists[dto.artistId]!!.apply {
                    name = dto.name
                    subName = dto.subName
                    nickname = dto.nickname
                    imageUrl = dto.imageUrl
                }
            } else {
                com.newket.infra.jpa.artist.entity.Artist(
                    name = dto.name,
                    subName = dto.subName,
                    nickname = dto.nickname,
                    imageUrl = dto.imageUrl
                )
            }
        }

        val artistsToDelete = existingArtists.filterKeys { !incomingIds.contains(it) }
        artistRemover.deleteAll(artistsToDelete.values.toList())

        artistAppender.saveAll(artistsToSave)
    }

    fun searchArtist(keyword: String): List<CreateTicketRequest.Artist> {
        return artistReader.autocompleteByKeyword(keyword).map { artist ->
            CreateTicketRequest.Artist(
                artistId = artist.id,
                name = "**${artist.name}** ${artist.subName ?: ""} ${artist.nickname ?: ""}",
            )
        }
    }

    //아티스트 크롤링
    fun fetchTicketArtist(text: String): List<CreateTicketRequest.Artist> {
        val artistList =
            artistReader.findAll().map { "${it.id} ${it.name} ${it.subName ?: ""} ${it.nickname ?: ""} " }.toString()
        return ticketGeminiClient.getArtists(text, artistList)
    }

    fun getAllGroups(): List<GroupTableDto> {
        return artistReader.findAllGroups().map {
            GroupTableDto(
                id = it.id,
                groupId = it.groupId,
                memberId = it.memberId,
            )
        }
    }

    @Transactional
    fun putAllGroups(groupList: List<GroupTableDto>) {
        val existingGroups = artistReader.findAllGroups().associateBy { it.id }
        val incomingIds = groupList.mapNotNull { it.id.takeIf { id -> id != 0L } }.toSet()

        val groupsToSave = groupList.map { dto ->
            if (dto.id != 0L && existingGroups.containsKey(dto.id)) {
                existingGroups[dto.id]!!.apply {
                    groupId = dto.groupId
                    memberId = dto.memberId
                }
            } else {
                GroupMember(
                    groupId = dto.groupId,
                    memberId = dto.memberId
                )
            }
        }

        val groupsToDelete = existingGroups.filterKeys { !incomingIds.contains(it) }
        artistRemover.deleteAllGroups(groupsToDelete.values.toList())

        artistAppender.saveAllGroups(groupsToSave)
    }

    // 장소DB
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
                existingPlaces[dto.id]!!.apply {
                    placeName = dto.placeName
                    url = dto.url
                }
            } else {
                Place(
                    placeName = dto.placeName,
                    url = dto.url
                )
            }
        }
        placeAppender.saveAll(placesToSave)
    }

    fun searchPlace(keyword: String): List<PlaceTableDto> {
        return placeReader.findByPlaceNameContaining(keyword).map {
            PlaceTableDto(
                id = it.id,
                placeName = it.placeName,
                url = it.url
            )
        }
    }

    // s3
    fun uploadFile(file: MultipartFile): String {
        val uuid = UUID.randomUUID().toString()
        val fileName = "lineup/${uuid}_${file.originalFilename}"
        val metadata = ObjectMetadata().apply {
            contentType = file.contentType
            contentLength = file.size
        }

        amazonS3Client.putObject(
            s3Properties.bucket,
            fileName,
            file.inputStream,
            metadata
        )

        return amazonS3Client.getUrl(s3Properties.bucket, fileName).toString()
    }

    //판매 중인 티켓 -> ticketCache
//    fun saveTicketCache() {
//        ticketCacheRemover.deleteAllTicketCache()
//        val ticketEventSchedules = ticketReader.findAllSellingTicket().groupBy { it.ticket }
//
//        val ticketCaches: List<TicketCache> = ticketEventSchedules.map { (ticket, eventSchedules) ->
//            val ticketSaleSchedules =
//                ticketReader.findAllTicketSaleScheduleByTicketId(ticket.id).sortedBy { it.time }.sortedBy { it.day }
//                    .map { Triple(it.type, it.day, it.time) }.distinct()
//
//            TicketCache(ticketId = ticket.id,
//                genre = ticket.genre,
//                imageUrl = ticket.imageUrl,
//                title = ticket.title,
//                customDate = DateUtil.dateToString(eventSchedules.map { it.day }),
//                ticketEventSchedules = eventSchedules.map {
//                    TicketEventSchedule(
//                        dateTime = LocalDateTime.of(it.day, it.time),
//                        customDateTime = DateUtil.dateTimeToString(it.day, it.time),
//                    )
//                },
//                ticketSaleSchedules = ticketSaleSchedules.map { ticketSaleSchedule ->
//                    TicketSaleSchedule(
//                        type = ticketSaleSchedule.first,
//                        dateTime = LocalDateTime.of(ticketSaleSchedule.second, ticketSaleSchedule.third)
//                    )
//                },
//                artists = artistReader.findAllByTicketId(ticket.id).map {
//                    Artist(
//                        artistId = it.id,
//                        name = it.name,
//                        subName = it.subName,
//                        nickname = it.nickname,
//                    )
//                })
//        }
//        ticketCacheAppender.saveAllTicketCache(ticketCaches)
//    }
}