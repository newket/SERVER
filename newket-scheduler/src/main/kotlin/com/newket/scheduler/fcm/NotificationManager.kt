package com.newket.scheduler.fcm

import com.newket.domain.artist.ArtistReader
import com.newket.domain.notification.NotificationAppender
import com.newket.domain.notification.NotificationReader
import com.newket.domain.ticket.TicketReader
import com.newket.domain.user.UserReader
import com.newket.infra.jpa.notifiacation.entity.Notification
import com.newket.infra.jpa.ticket_artist.entity.TicketArtist
import com.newket.scheduler.event.FcmNotificationEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalTime

@Component
class NotificationManager(
    private val artistReader: ArtistReader,
    private val userReader: UserReader,
    private val ticketReader: TicketReader,
    private val notificationReader: NotificationReader,
    private val notificationAppender: NotificationAppender,
    private val eventPublisher: ApplicationEventPublisher,
) {

    fun sendTicketNotification(ticketArtists: List<TicketArtist>) {
        ticketArtists.map { ticketArtist ->
            artistReader.findAllFavoriteArtistsByArtistId(ticketArtist.artist.id).map {
                userReader.findUserDeviceByUserId(it.userId).map { user ->
                    if (user.artistNotification) {
                        // 1. 알림 DB 저장
                        val notification = Notification(
                            userId = user.userId,
                            title = "${ticketArtist.artist.name}의 새 티켓 소식이 도착했어요!",
                            content = "지금 눌러서 ${ticketArtist.ticket.title} 티켓을 확인해보세요!",
                            isOpened = false
                        ).run {
                            notificationAppender.addNotification(this)
                        }
                        // 2. 트랜잭션 커밋 이후 FCM 전송 이벤트 발행
                        eventPublisher.publishEvent(
                            FcmNotificationEvent(
                                token = user.token,
                                title = notification.title,
                                body = notification.content,
                                ticketId = ticketArtist.ticket.id.toString(),
                                notificationId = notification.id.toString()
                            )
                        )
                    }
                }
            }
        }
    }

    fun sendTicketOpenBefore1hour() {
        val date = LocalDate.now()
        val time = LocalTime.now()
        ticketReader.findAllTicketSaleScheduleByDateAndTime(date, time.plusHours(1)).groupBy { it.ticketSaleUrl.ticket }
            .map { (ticket, schedule) ->
                notificationReader.findAllTicketNotificationByTicketId(ticket.id)
                    .map { notification ->
                        userReader.findUserDeviceByUserId(notification.userId).map { user ->
                            if (user.ticketNotification) {
                                val newNotification = Notification(
                                    userId = user.userId,
                                    title = "티켓 오픈 1시간 전이에요!",
                                    content = "${ticket.title}의 ${schedule.first().type}가 1시간 뒤에 오픈돼요.",
                                    isOpened = false
                                ).run {
                                    notificationAppender.addNotification(this)
                                }
                                eventPublisher.publishEvent(
                                    FcmNotificationEvent(
                                        token = user.token,
                                        title = newNotification.title,
                                        body = newNotification.content,
                                        ticketId = ticket.id.toString(),
                                        notificationId = notification.id.toString()
                                    )
                                )
                            }
                        }
                    }
            }
    }

    fun sendTicketOpenBeforeDay() {
        val date = LocalDate.now()
        val time = LocalTime.now()
        ticketReader.findAllTicketSaleScheduleByDateAndTime(date.plusDays(1), time).groupBy { it.ticketSaleUrl.ticket }
            .map { (ticket, schedule) ->
                notificationReader.findAllTicketNotificationByTicketId(ticket.id)
                    .map { notification ->
                        userReader.findUserDeviceByUserId(notification.userId).map { user ->
                            if (user.ticketNotification) {
                                val newNotification = Notification(
                                    userId = user.userId,
                                    title = "티켓 오픈 하루 전이에요!",
                                    content = "${ticket.title}의 ${schedule.first().type}가 내일 오픈돼요.",
                                    isOpened = false
                                ).run {
                                    notificationAppender.addNotification(this)
                                }
                                eventPublisher.publishEvent(
                                    FcmNotificationEvent(
                                        token = user.token,
                                        title = newNotification.title,
                                        body = newNotification.content,
                                        ticketId = ticket.id.toString(),
                                        notificationId = notification.id.toString()
                                    )
                                )
                            }
                        }
                    }
            }
    }
}