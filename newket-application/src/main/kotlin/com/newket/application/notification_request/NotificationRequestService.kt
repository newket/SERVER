package com.newket.application.notification_request

import com.newket.application.artist.dto.common.ArtistDto
import com.newket.application.notification_request.dto.ArtistNotificationResponse
import com.newket.application.ticket.dto.BeforeSaleTicketsResponse
import com.newket.application.ticket.dto.OnSaleResponse
import com.newket.core.auth.getCurrentUserId
import com.newket.core.util.DateUtil
import com.newket.domain.artist.ArtistReader
import com.newket.domain.notification_request.NotificationRequestAppender
import com.newket.domain.notification_request.NotificationRequestReader
import com.newket.domain.notification_request.NotificationRequestRemover
import com.newket.domain.ticket_cache.TicketCacheReader
import com.newket.infra.jpa.notification_request.entity.ArtistNotification
import com.newket.infra.jpa.notification_request.entity.TicketNotification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class NotificationRequestService(
    private val artistReader: ArtistReader,
    private val notificationRequestReader: NotificationRequestReader,
    private val notificationRequestAppender: NotificationRequestAppender,
    private val notificationRequestRemover: NotificationRequestRemover,
    private val ticketCacheReader: TicketCacheReader,
) {
    // 아티스트 알림 추가
    @Transactional
    fun postArtistNotification(artistId: Long) {
        val userId = getCurrentUserId()
        notificationRequestAppender.saveArtistNotification(ArtistNotification(userId = userId, artistId = artistId))
    }

    // 아티스트 알림 여부
    fun isArtistNotification(artistId: Long): Boolean {
        val userId = getCurrentUserId()
        notificationRequestReader.findArtistNotificationOrNull(userId, artistId)?.let { return true } ?: return false
    }

    // 아티스트 알림 삭제
    @Transactional
    fun deleteArtistNotification(artistId: Long) {
        val userId = getCurrentUserId()
        notificationRequestRemover.deleteArtistNotification(userId, artistId)
    }

    // 티켓 알림 추가
    @Transactional
    fun postTicketNotification(ticketId: Long) {
        val userId = getCurrentUserId()
        notificationRequestAppender.saveTicketNotification(
            TicketNotification(
                userId = userId,
                ticketId = ticketId,
            )
        )
    }

    // 티켓 알림 여부
    fun isTicketNotification(ticketId: Long): Boolean {
        val userId = getCurrentUserId()
        notificationRequestReader.findTicketNotificationOrNull(ticketId, userId)?.let {
            return true
        } ?: return false
    }

    // 아티스트 알림 불러오기
    fun getAllArtistNotification(): ArtistNotificationResponse {
        val userId = getCurrentUserId()

        return ArtistNotificationResponse(notificationRequestReader.findAllArtistNotification(userId).map {
            val artist = artistReader.findById(it.artistId)
            ArtistDto(
                artistId = it.artistId, name = artist.name, subName = artist.subName, imageUrl = artist.imageUrl
            )
        })
    }

    // 아티스트 알림받는 오픈 예정 티켓
    fun getAllArtistBeforeSaleTicket(): BeforeSaleTicketsResponse {
        val userId = getCurrentUserId()
        val artistIds = notificationRequestReader.findAllArtistNotification(userId).map { it.artistId }
        val tickets = ticketCacheReader.findAllBeforeSaleTicketByArtistIds(artistIds).map {
            it.copy(ticketSaleSchedules = it.ticketSaleSchedules.filter { schedule ->
                schedule.dateTime.isAfter(LocalDateTime.now()) || schedule.dateTime.isEqual(LocalDateTime.now())
            })
        }.sortedBy { ticket -> ticket.ticketSaleSchedules.first().dateTime }

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

    // 아티스트 알림받는 예매 중인 티켓
    fun getAllArtistOnSaleTicket(): OnSaleResponse {
        val userId = getCurrentUserId()
        val artistIds = notificationRequestReader.findAllArtistNotification(userId).map { it.artistId }
        val tickets = ticketCacheReader.findAllOnSaleTicketByArtistIds(artistIds)
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

    // 티켓 알림 불러오기 - 오픈 예정 티켓
    fun getAllTicketNotification(): BeforeSaleTicketsResponse {
        val userId = getCurrentUserId()
        val ticketIds = notificationRequestReader.findAllTicketNotification(userId).map { it.ticketId }
        val tickets = ticketCacheReader.findAllBeforeSaleTicketByTicketIds(ticketIds).map {
            it.copy(ticketSaleSchedules = it.ticketSaleSchedules.filter { schedule ->
                schedule.dateTime.isAfter(LocalDateTime.now()) || schedule.dateTime.isEqual(LocalDateTime.now())
            })
        }.sortedBy { ticket -> ticket.ticketSaleSchedules.first().dateTime }

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

    // 티켓 삭제
    @Transactional
    fun deleteTicketNotification(ticketId: Long) {
        val userId = getCurrentUserId()
        notificationRequestRemover.deleteTicketNotification(ticketId, userId)
    }

}