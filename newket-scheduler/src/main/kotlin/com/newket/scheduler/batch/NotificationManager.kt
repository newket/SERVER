package com.newket.scheduler.batch

import com.fasterxml.jackson.databind.ObjectMapper
import com.newket.client.fcm.FcmClient
import com.newket.client.fcm.FcmMessageDto
import com.newket.domain.artist.ArtistReader
import com.newket.domain.notification.NotificationAppender
import com.newket.domain.notification.NotificationReader
import com.newket.domain.ticket.service.TicketReader
import com.newket.domain.user.service.UserReader
import com.newket.infra.jpa.notifiacation.entity.Notification
import com.newket.infra.jpa.ticket_artist.entity.TicketArtist
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalTime

@Component
class NotificationManager(
    private val artistReader: ArtistReader,
    private val userReader: UserReader,
    private val fcmClient: FcmClient,
    private val ticketReader: TicketReader,
    private val notificationReader: NotificationReader,
    private val notificationAppender: NotificationAppender
) {

    fun sendFavoriteTicketOpeningNotice(ticketArtists: List<TicketArtist>) {
        ticketArtists.map { ticketArtist ->
            artistReader.findAllFavoriteArtistsByArtistId(ticketArtist.artist.id).map {
                userReader.findUserDeviceByUserId(it.userId).map { user ->
                    //artistNotification 이 켜저 있는 유저
                    if (user.artistNotification) {
                        val om = ObjectMapper()
                        val notification = Notification(
                            userId = user.userId,
                            title = "${ticketArtist.artist.name}의 새 티켓 소식이 도착했어요!",
                            content = "지금 눌러서 ${ticketArtist.ticket.title} 티켓을 확인해보세요!",
                            isOpened = false
                        ).run {
                            notificationAppender.addNotification(this)
                        }
                        val fcmMessageDto = FcmMessageDto(
                            message = FcmMessageDto.Message(
                                token = user.token,
                                notification = FcmMessageDto.Notification(
                                    title = notification.title,
                                    body = notification.content,
                                ),
                                data = mapOf(
                                    "ticketId" to ticketArtist.ticket.id.toString(),
                                    "notificationId" to notification.id.toString(),
                                )
                            )
                        )
                        fcmClient.sendMessage(om.writeValueAsString(fcmMessageDto))
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
                            //ticketNotification 이 켜저 있는 유저
                            if (user.ticketNotification) {
                                val om = ObjectMapper()
                                val newNotification = Notification(
                                    userId = user.userId,
                                    title = "티켓 오픈 1시간 전이에요!",
                                    content = "${ticket.title}의 ${schedule.first().type}가 1시간 뒤에 오픈돼요.",
                                    isOpened = false
                                ).run {
                                    notificationAppender.addNotification(this)
                                }
                                val fcmMessageDto = FcmMessageDto(
                                    message = FcmMessageDto.Message(
                                        token = user.token,
                                        notification = FcmMessageDto.Notification(
                                            title = newNotification.title,
                                            body = newNotification.content,
                                        ),
                                        data = mapOf(
                                            "ticketId" to ticket.id.toString(),
                                            "notificationId" to notification.id.toString()
                                        )
                                    )
                                )
                                fcmClient.sendMessage(om.writeValueAsString(fcmMessageDto))
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
                            //ticketNotification 이 켜저 있는 유저
                            if (user.ticketNotification) {
                                val om = ObjectMapper()
                                val newNotification = Notification(
                                    userId = user.userId,
                                    title = "티켓 오픈 하루 전이에요!",
                                    content = "${ticket.title}의 ${schedule.first().type}가 내일 오픈돼요.",
                                    isOpened = false
                                ).run {
                                    notificationAppender.addNotification(this)
                                }
                                val fcmMessageDto = FcmMessageDto(
                                    message = FcmMessageDto.Message(
                                        token = user.token,
                                        notification = FcmMessageDto.Notification(
                                            title = newNotification.title,
                                            body = newNotification.content,
                                        ),
                                        data = mapOf(
                                            "ticketId" to ticket.id.toString(),
                                            "notificationId" to notification.id.toString()
                                        )
                                    )
                                )
                                fcmClient.sendMessage(om.writeValueAsString(fcmMessageDto))
                            }
                        }
                    }
            }
    }
}