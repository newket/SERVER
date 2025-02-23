package com.newket.domain.notification

import com.newket.infra.jpa.notifiacation.entity.Notification
import com.newket.infra.jpa.notifiacation.repository.NotificationRepository
import org.springframework.stereotype.Service

@Service
class NotificationAppender(
    private val notificationRepository: NotificationRepository
) {
    fun addNotification(notification: Notification) = notificationRepository.save(notification)
}