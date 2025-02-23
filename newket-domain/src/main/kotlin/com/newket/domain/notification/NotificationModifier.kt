package com.newket.domain.notification

import com.newket.infra.jpa.notifiacation.entity.Notification
import com.newket.infra.jpa.notifiacation.repository.NotificationRepository
import org.springframework.stereotype.Service

@Service
class NotificationModifier(
    val notificationRepository: NotificationRepository
) {
    fun updateIsOpened(notification: Notification) {
        notification.updateIsOpened()
        notificationRepository.save(notification)
    }
}