package com.newket.application.notification

import com.newket.domain.notification.NotificationModifier
import com.newket.domain.notification.NotificationReader
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Service
class NotificationService(
    private val notificationReader: NotificationReader,
    private val notificationModifier: NotificationModifier,
) {

    @Transactional
    fun updateNotificationIsOpened(notificationId: Long) {
        val notification = notificationReader.findById(notificationId)
        notificationModifier.updateIsOpened(notification)
    }
}