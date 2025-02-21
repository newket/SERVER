package com.newket.application.artist

import com.newket.application.artist.dto.Artist
import com.newket.application.artist.dto.ArtistProfile
import com.newket.application.artist.dto.ArtistRequest
import com.newket.application.artist.dto.FavoriteArtists
import com.newket.application.ticket.dto.OnSale
import com.newket.application.ticket.dto.SearchResult
import com.newket.client.slack.SlackClient
import com.newket.core.auth.getCurrentUserId
import com.newket.core.util.DateUtil
import com.newket.domain.artist.service.ArtistAppender
import com.newket.domain.artist.service.ArtistReader
import com.newket.domain.artist.service.ArtistRemover
import com.newket.domain.ticket.service.TicketReader
import com.newket.domain.ticket_cache.service.TicketCacheReader
import com.newket.infra.jpa.notifiacation.entity.ArtistNotification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Transactional(readOnly = true)
@Service
class ArtistService(
    private val artistReader: ArtistReader,
    private val artistAppender: ArtistAppender,
    private val slackClient: SlackClient,
    private val artistRemover: ArtistRemover,
    private val ticketReader: TicketReader,
    private val ticketCacheReader: TicketCacheReader
) {
    fun requestArtistV2(request: ArtistRequest.Request) {
        slackClient.sendSlackMessage(
            "artist: ${request.artistName}\nartistInfo: ${request.artistInfo}\ndeviceToken: ${request.deviceToken}",
            "artist_request"
        )
    }

    fun getFavoriteArtists(): FavoriteArtists.Response {
        val userId = getCurrentUserId()

        return FavoriteArtists.Response(artistReader.findAllFavoriteArtistsByUserId(userId).map {
            val artist = artistReader.findById(it.artistId).orElseThrow()
            FavoriteArtists.Artist(
                name = artist.name, nicknames = artist.subName, artistId = it.artistId
            )
        })
    }

    fun getFavoriteArtist(artistId: Long): Boolean {
        val userId = getCurrentUserId()
        artistReader.findFavoriteArtistByUserIdAndArtistId(userId, artistId)?.let { return true } ?: return false
    }

    @Transactional
    fun addFavoriteArtist(artistId: Long) {
        val userId = getCurrentUserId()
        artistAppender.addUserFavoriteArtist(ArtistNotification(userId = userId, artistId = artistId))
    }

    @Transactional
    fun deleteFavoriteArtist(artistId: Long) {
        val userId = getCurrentUserId()
        artistRemover.deleteUserFavoriteArtistByUserId(userId, artistId)
    }

    fun getArtistProfile(artistId: Long): ArtistProfile.Response {
        val artist = artistReader.findById(artistId).orElseThrow()
        val members = artistReader.findAllMembersByGroupId(artistId)
        val groups = artistReader.findAllGroupsByMemberId(artistId)
        val beforeSaleTickets = ticketCacheReader.findAllBeforeSaleTicketByArtistId(artistId).map {
            it.copy(ticketSaleSchedules = it.ticketSaleSchedules.filter { schedule ->
                schedule.dateTime.isAfter(LocalDateTime.now()) || schedule.dateTime.isEqual(LocalDateTime.now())
            })
        }
        val onSaleTickets = ticketCacheReader.findAllOnSaleTicketByArtistId(artistId)

        val afterSaleConcerts = ticketReader.findAllAfterSaleByArtistId(artistId).groupBy {
            it.ticket
        }

        return ArtistProfile.Response(
            info = Artist(
                artistId = artistId, name = artist.name, subName = artist.subName, imageUrl = artist.imageUrl,
            ),
            members = members.map {
                Artist(artistId = it.id, name = it.name, subName = it.subName, imageUrl = it.imageUrl)
            },
            groups = groups.map {
                Artist(artistId = it.id, name = it.name, subName = it.subName, imageUrl = it.imageUrl)
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
                }),
            afterSale = OnSale.Response(
                totalNum = afterSaleConcerts.size,
                concerts = afterSaleConcerts.map { (concert, schedules) ->
                    OnSale.Concert(
                        concertId = concert.id,
                        imageUrl = concert.imageUrl,
                        title = concert.title,
                        date = DateUtil.dateToString(schedules.map { it.day }
                        )
                    )
                }.let { concertList -> concertList.sortedBy { it.date } }
            )
        )
    }

    fun getRandomArtists(): List<Artist> {
        return artistReader.findRandomArtists().map { artist ->
            Artist(
                artistId = artist.id,
                name = artist.name,
                subName = artist.subName,
                imageUrl = artist.imageUrl,
            )
        }
    }
}