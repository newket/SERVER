package com.newket.application.admin

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import com.newket.application.admin.dto.*
import com.newket.application.artist.dto.common.ArtistDto
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
import java.time.LocalDateTime
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
) {
    suspend fun fetchTicket(url: String): CreateTicketRequest = coroutineScope {
        val ticketInfoDeferred = async { ticketCrawlingClient.fetchTicketInfo(url) }
        val ticketRawDeferred = async { ticketCrawlingClient.fetchTicketRaw(url) }
        val artistListDeferred = async {
            artistReader.findAll().map { "${it.id} ${it.name} ${it.subName ?: ""} ${it.nickname ?: ""}" }.toString()
        }
        val placeListDeferred = async {
            placeReader.findAll().map { it.placeName }.toString()
        }

        val ticketInfo = withTimeout(2 * 60 * 1000) { ticketInfoDeferred.await() }
        val ticketRaw = withTimeout(2 * 60 * 1000) { ticketRawDeferred.await() }
        val artistList = artistListDeferred.await()
        val placeList = placeListDeferred.await()

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
        val ticketInfoDeferred = async { ticketCrawlingClient.fetchTicketInfo(url) }
        val ticketRawDeferred = async { ticketCrawlingClient.fetchTicketRaw(url) }
        val artistListDeferred = async {
            artistReader.findAll().map { "${it.id} ${it.name} ${it.subName ?: ""} ${it.nickname ?: ""}" }.toString()
        }
        val placeListDeferred = async {
            placeReader.findAll().map { it.placeName }.toString()
        }

        val ticketInfo = withTimeout(2 * 60 * 1000) { ticketInfoDeferred.await() }
        val ticketRaw = withTimeout(2 * 60 * 1000) { ticketRawDeferred.await() }
        val artistList = artistListDeferred.await()
        val placeList = placeListDeferred.await()

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
            ticketAppender.saveLineupImage(TicketLineup(ticket = ticket, imageUrl = it))
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
    fun createMusical(request: CreateMusicalRequest) {
        //mysql
        val artists = request.artists.map {
            artistReader.findById(it.artistId)
        }
        val ticket = Ticket(
            place = placeReader.findByPlaceName(request.place!!),
            title = request.title,
            imageUrl = request.imageUrl,
            genre = Genre.MUSICAL
        )
        ticketAppender.saveTicket(ticket)
        val ticketArtists = artists.map { artist ->
            TicketArtist(artist = artist, ticket = ticket)
        }
        val savedTicketArtists = ticketAppender.saveAllTicketArtist(ticketArtists)

        val musicalArtists = savedTicketArtists.mapIndexed { index, ticketArtist ->
            val requestArtist = request.artists[index]
            MusicalArtist(
                ticketArtistId = ticketArtist.id,
                role = requestArtist.role
            )
        }
        ticketArtistAppender.saveAllMusicalArtists(musicalArtists)


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
        if (!request.lineupImage.isNullOrEmpty()) {
            ticketAppender.saveLineupImage(TicketLineup(ticket = ticket, imageUrl = request.lineupImage!!))
        }
        request.price.map {
            ticketAppender.saveTicketPrice(TicketPrice(ticket = ticket, type = it.type, price = it.price))
        }


        //mongo
        val ticketSaleSchedules =
            ticketSaleScheduleList.sortedBy { it.time }.sortedBy { it.day }.map { Triple(it.type, it.day, it.time) }
                .distinct()

        val ticketCache = TicketCache(ticketId = ticket.id,
            genre = Genre.MUSICAL,
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
        ticketCacheAppender.saveTicketCache(ticketCache)
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


    fun getMusical(): List<TicketTableResponse> {
        return ticketCacheReader.findAllMusicalTicket().map { musicalTicket ->
            val ticket = ticketReader.findTicketById(musicalTicket.ticketId)
            val ticketId = musicalTicket.ticketId
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
    fun deleteMusical(ticketId: Long) {
        ticketCacheRemover.deleteByTicketId(ticketId)
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