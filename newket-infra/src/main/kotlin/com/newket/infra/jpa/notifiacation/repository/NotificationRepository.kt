package com.newket.infra.jpa.notifiacation.repository

import com.newket.infra.jpa.notifiacation.entity.Notification
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationRepository : JpaRepository<Notification, Long> {
    fun findAllByUserId(userId: Long): List<Notification>
}