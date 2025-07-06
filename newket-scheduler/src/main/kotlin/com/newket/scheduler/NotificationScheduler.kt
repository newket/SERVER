package com.newket.scheduler

import com.newket.scheduler.batch.NotificationManager
import com.newket.scheduler.batch.TicketCacheManager
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class NotificationScheduler(
    private val notificationManager: NotificationManager,
    private val ticketCacheManager: TicketCacheManager
) {
//    // 추가 예매
//    @Scheduled(cron = "0 00 10 * * *") //매일 오전 10시
//    fun addTicketSale() {
//        ticketCacheManager.updateTicketSaleAndSendArtistNotification()
//    }
//
//    // 아티스트 추가
//    @Scheduled(cron = "0 00 14 * * *") //매일 오후 2시
//    fun addTicketArtist() {
//        ticketCacheManager.updateTicketArtistAndSendArtistNotification()
//    }
//
//    // 새로운 티켓
//    @Scheduled(cron = "0 00 14 * * *") //매일 오후 2시
//    fun newTicketOpen() {
//        ticketCacheManager.createTicketCacheAndSendArtistNotification()
//    }
//
//    @Scheduled(cron = "0 00 * * * *") //매시간
//    fun ticketOpenBefore1hour() {
//        notificationManager.sendTicketOpenBefore1hour()
//        notificationManager.sendTicketOpenBeforeDay()
//    }
//
//    @Scheduled(cron = "0 0 0 * * *") // 매일 자정 12시
//    fun deleteAllOldTicketCache() {
//        ticketCacheManager.deleteAllAfterSaleTicketCache()
//    }
}