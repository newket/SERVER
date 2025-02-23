package com.newket.application.notification_request

import com.newket.application.artist.dto.common.ArtistDto
import com.newket.application.notification_request.dto.ArtistNotificationResponse
import com.newket.application.ticket.dto.BeforeSaleTicketsResponse
import com.newket.core.auth.getCurrentUserId
import com.newket.core.util.DateUtil
import com.newket.domain.artist.ArtistReader
import com.newket.domain.notification_request.NotificationRequestAppender
import com.newket.domain.notification_request.NotificationRequestReader
import com.newket.domain.notification_request.NotificationRequestRemover
import com.newket.domain.ticket.service.TicketReader
import com.newket.infra.jpa.notification_request.entity.ArtistNotification
import com.newket.infra.jpa.notification_request.entity.TicketNotification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NotificationRequestService(
    private val ticketReader: TicketReader,
    private val artistReader: ArtistReader,
    private val notificationRequestReader: NotificationRequestReader,
    private val notificationRequestAppender: NotificationRequestAppender,
    private val notificationRequestRemover: NotificationRequestRemover,
) {
    // 아티스트 알림 추가
    @Transactional
    fun postArtistNotification(artistId: Long) {
        val userId = getCurrentUserId()
        notificationRequestAppender.saveArtistNotification(ArtistNotification(userId = userId, artistId = artistId))
    }

    // 아티스트 알림 불러오기
    fun getArtistNotifications(): ArtistNotificationResponse {
        val userId = getCurrentUserId()

        return ArtistNotificationResponse(artistReader.findAllFavoriteArtistsByUserId(userId).map {
            val artist = artistReader.findById(it.artistId).orElseThrow()
            ArtistDto(
                artistId = it.artistId, name = artist.name, subName = artist.subName, imageUrl = artist.imageUrl
            )
        })
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

    // 티켓 알림 불러오기 - 오픈 예정 티켓
    fun getAllTicketNotification(): BeforeSaleTicketsResponse {
        val userId = getCurrentUserId()
        val tickets = ticketReader.findAllTicketNotificationSaleSchedule(userId).groupBy {
            it.ticketSaleUrl.ticket
        }
        return BeforeSaleTicketsResponse(
            totalNum = tickets.keys.size,
            tickets = tickets.map { (ticket, schedules) ->
                BeforeSaleTicketsResponse.BeforeSaleTicketDto(
                    ticketId = ticket.id,
                    imageUrl = ticket.imageUrl,
                    title = ticket.title,
                    ticketSaleSchedules = schedules.map {
                        BeforeSaleTicketsResponse.TicketSaleScheduleDto(
                            type = it.type,
                            dDay = DateUtil.dateToDDay(it.day)
                        )
                    }.toSet().toList()
                )
            }
        )
    }

    // 티켓 알림 여부
    fun isTicketNotification(ticketId: Long): Boolean {
        val userId = getCurrentUserId()
        notificationRequestReader.findTicketNotificationOrNull(ticketId, userId)?.let {
            return true
        } ?: return false
    }

    // 티켓 삭제
    @Transactional
    fun deleteTicketNotification(ticketId: Long) {
        val userId = getCurrentUserId()
        notificationRequestRemover.deleteTicketNotification(ticketId, userId)
    }
}