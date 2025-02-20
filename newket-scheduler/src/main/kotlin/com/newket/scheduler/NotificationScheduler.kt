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
    @Scheduled(cron = "0 00 14 * * *") //매일 오후 2시
    fun newTicketOpen() {
        ticketCacheManager.saveTicketCacheAndSendArtistNotification()
    }

    @Scheduled(cron = "0 00 * * * *") //매시간
    fun ticketOpenBefore1hour() {
        notificationManager.sendTicketOpenBefore1hour()
        notificationManager.sendTicketOpenBeforeDay()
    }

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정 12시
    fun deleteAllOldTicketCache() {
        ticketCacheManager.deleteAllOldTicketCache()
    }
}