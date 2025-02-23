package com.newket.api.controller.notification_request

import com.newket.application.notification_request.NotificationRequestService
import com.newket.application.notification_request.dto.ArtistNotificationResponse
import com.newket.application.ticket.dto.BeforeSaleTicketsResponse
import com.newket.application.ticket.dto.OnSaleResponse
import org.springframework.web.bind.annotation.*

@RestController
class NotificationRequestController(
    private val notificationRequestService: NotificationRequestService,
) {
    // 아티스트 알림 추가
    @PostMapping(NotificationRequestApi.V1.ARTIST_DETAIL)
    fun postArtistNotification(@PathVariable artistId: Long) {
        return notificationRequestService.postArtistNotification(artistId)
    }

    // 아티스트 알림 여부
    @GetMapping(NotificationRequestApi.V1.ARTIST_DETAIL)
    fun isArtistNotification(@PathVariable artistId: Long): Boolean {
        return notificationRequestService.isArtistNotification(artistId)
    }

    // 아티스트 알림 삭제
    @DeleteMapping(NotificationRequestApi.V1.ARTIST_DETAIL)
    fun deleteArtistNotification(@PathVariable artistId: Long) {
        return notificationRequestService.deleteArtistNotification(artistId)
    }

    // 티켓 알림 추가
    @PostMapping(NotificationRequestApi.V1.TICKET_DETAIL)
    fun postTicketNotification(@PathVariable ticketId: Long) {
        return notificationRequestService.postTicketNotification(ticketId)
    }

    // 티켓 알림 여부
    @GetMapping(NotificationRequestApi.V1.TICKET_DETAIL)
    fun isTicketNotification(@PathVariable ticketId: Long): Boolean {
        return notificationRequestService.isTicketNotification(ticketId)
    }

    // 티켓 삭제
    @DeleteMapping(NotificationRequestApi.V1.TICKET_DETAIL)
    fun deleteTicketNotification(@PathVariable ticketId: Long) {
        return notificationRequestService.deleteTicketNotification(ticketId)
    }

    // 아티스트 알림 불러오기
    @GetMapping(NotificationRequestApi.V1.ARTIST)
    fun getAllArtistNotification(): ArtistNotificationResponse {
        return notificationRequestService.getAllArtistNotification()
    }

    // 아티스트 알림받는 오픈 예정 티켓
    @GetMapping(NotificationRequestApi.V1.ARTIST_BEFORE_SALE)
    fun getAllBeforeSaleTicketNotification(): BeforeSaleTicketsResponse {
        return notificationRequestService.getAllArtistBeforeSaleTicket()
    }

    // 아티스트 알림받는 예매 중인 티켓
    @GetMapping(NotificationRequestApi.V1.ARTIST_ON_SALE)
    fun getAllArtistOnSaleTicket(): OnSaleResponse {
        return notificationRequestService.getAllArtistOnSaleTicket()
    }

    // 티켓 알림 불러오기 - 오픈 예정 티켓
    @GetMapping(NotificationRequestApi.V1.TICKET)
    fun getAllTicketNotification(): BeforeSaleTicketsResponse {
        return notificationRequestService.getAllTicketNotification()
    }
}