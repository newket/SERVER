package com.newket.application.notification

import com.newket.application.notification.dto.ConcertIds
import com.newket.application.notification.dto.Notifications
import com.newket.application.ticket.dto.OpeningNotice
import com.newket.core.auth.getCurrentUserId
import com.newket.core.util.DateUtil
import com.newket.domain.artist.service.ArtistReader
import com.newket.domain.ticket.service.TicketReader
import com.newket.domain.notifiacation.service.NotificationAppender
import com.newket.domain.notifiacation.service.NotificationModifier
import com.newket.domain.notifiacation.service.NotificationReader
import com.newket.domain.notifiacation.service.NotificationRemover
import com.newket.infra.jpa.notifiacation.entity.TicketNotification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Service
class NotificationService(
    private val notificationAppender: NotificationAppender,
    private val notificationRemover: NotificationRemover,
    private val notificationReader: NotificationReader,
    private val notificationModifier: NotificationModifier,
    private val artistReader: ArtistReader,
    private val ticketReader: TicketReader
) {
    //티켓 알림 설정
    @Transactional
    fun addTicketNotification(concertId: Long) {
        val userId = getCurrentUserId()
        notificationAppender.addUserFavoriteTicket(
            TicketNotification(
                userId = userId,
                ticketId = concertId,
            )
        )
    }

    @Transactional
    fun deleteTicketNotification(concertId: Long) {
        val userId = getCurrentUserId()
        notificationRemover.deleteNotificationTicketRequestByConcertIdAndUserId(concertId, userId)
    }

    fun getIsTicketNotification(concertId: Long): Boolean {
        val userId = getCurrentUserId()
        notificationReader.findNotificationTicketOrNull(concertId, userId)?.let {
            return true
        } ?: return false
    }

    fun getAllTicketNotification(): OpeningNotice.Response {
        val userId = getCurrentUserId()
        val concerts = ticketReader.findAllNotificationConcertTicketingSchedulesByUserId(userId).groupBy {
            it.ticketSaleUrl.ticket
        }
        return OpeningNotice.Response(
            totalNum = concerts.keys.size,
            artistName = if (concerts.isNotEmpty()) {
                artistReader.findAllByTicketId(concerts.keys.first().id).firstOrNull()?.name ?: "NONE"
            } else {
                "NONE"
            },
            concerts = concerts.map { (concert, schedules) ->
                OpeningNotice.Concert(
                    concertId = concert.id,
                    imageUrl = concert.imageUrl,
                    title = concert.title,
                    ticketingSchedules = schedules.map {
                        OpeningNotice.ConcertTicketingSchedule(
                            type = it.type,
                            dDay = DateUtil.dateToDDay(it.day)
                        )
                    }.toSet().toList()
                )
            }
        )
    }

    @Transactional
    fun deleteAllTicketNotifications(request: ConcertIds.Request) {
        val userId = getCurrentUserId()
        notificationRemover.deleteAllNotificationTicketRequestByConcertIdInAndUserId(request.concertIds, userId)
    }

    @Transactional
    fun updateNotificationIsOpened(notificationId: Long) {
        val notification = notificationReader.findById(notificationId)
        notificationModifier.updateIsOpened(notification)
    }

    fun getAllNotifications(): Notifications.Response {
        val userId = getCurrentUserId()
        return Notifications.Response(
            notifications = notificationReader.findAllByUserId(userId).groupBy {
                it.content
            }.map {
                Notifications.Notification(
                    title = it.value.first().title,
                    content = it.value.first().content
                )
            }
        )
    }
}