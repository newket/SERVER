package com.newket.application.artist

import com.newket.application.artist.dto.common.ArtistDto
import com.newket.application.artist.dto.ArtistProfileResponse
import com.newket.application.artist.dto.ArtistRequest
import com.newket.application.ticket.dto.BeforeSaleTicketsResponse
import com.newket.application.ticket.dto.OnSaleResponse
import com.newket.client.slack.SlackClient
import com.newket.core.util.DateUtil
import com.newket.domain.artist.ArtistReader
import com.newket.domain.ticket.service.TicketReader
import com.newket.domain.ticket_cache.TicketCacheReader
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Transactional(readOnly = true)
@Service
class ArtistService(
    private val artistReader: ArtistReader,
    private val slackClient: SlackClient,
    private val ticketReader: TicketReader,
    private val ticketCacheReader: TicketCacheReader
) {
    // 아티스트 등록 요청
    fun requestArtist(request: ArtistRequest) {
        slackClient.sendSlackMessage(
            "artist: ${request.artistName}\nartistInfo: ${request.artistInfo}\ndeviceToken: ${request.deviceToken}",
            "artist_request"
        )
    }

    // 아티스트 프로필
    fun getArtistProfile(artistId: Long): ArtistProfileResponse {
        val artist = artistReader.findById(artistId)
        val members = artistReader.findAllMembersByGroupId(artistId)
        val groups = artistReader.findAllGroupsByMemberId(artistId)
        val beforeSaleTickets = ticketCacheReader.findAllBeforeSaleTicketByArtistId(artistId).map {
            it.copy(ticketSaleSchedules = it.ticketSaleSchedules.filter { schedule ->
                schedule.dateTime.isAfter(LocalDateTime.now()) || schedule.dateTime.isEqual(LocalDateTime.now())
            })
        }
        val onSaleTickets = ticketCacheReader.findAllOnSaleTicketByArtistId(artistId)

        val afterSaleTickets = ticketReader.findAllAfterSaleByArtistId(artistId).groupBy {
            it.ticket
        }

        return ArtistProfileResponse(
            info = ArtistDto(
                artistId = artistId, name = artist.name, subName = artist.subName, imageUrl = artist.imageUrl,
            ),
            members = members.map {
                ArtistDto(artistId = it.id, name = it.name, subName = it.subName, imageUrl = it.imageUrl)
            },
            groups = groups.map {
                ArtistDto(artistId = it.id, name = it.name, subName = it.subName, imageUrl = it.imageUrl)
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
                        }.toSet().toList()
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
                }),
            afterSaleTickets = OnSaleResponse(
                totalNum = afterSaleTickets.size,
                tickets = afterSaleTickets.map { (ticket, schedules) ->
                    OnSaleResponse.OnSaleTicketDto(
                        ticketId = ticket.id,
                        imageUrl = ticket.imageUrl,
                        title = ticket.title,
                        date = DateUtil.dateToString(schedules.map { it.day }
                        )
                    )
                }
            )
        )
    }

    // 아티스트 랜덤 추천
    fun getRandomArtists(): List<ArtistDto> {
        return artistReader.findRandomArtists().map { artist ->
            ArtistDto(
                artistId = artist.id,
                name = artist.name,
                subName = artist.subName,
                imageUrl = artist.imageUrl,
            )
        }
    }
}