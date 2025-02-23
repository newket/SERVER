package com.newket.infra.jpa.notification_request.repository

import com.newket.infra.jpa.notification_request.entity.TicketNotification
import org.springframework.data.jpa.repository.JpaRepository

interface TicketNotificationRepository : JpaRepository<TicketNotification, Long> {
    fun findByTicketIdAndUserId(ticketId: Long, userId: Long): TicketNotification?

    fun deleteByTicketIdAndUserId(ticketId: Long, userId: Long)

    fun findAllByTicketId(ticketId: Long): List<TicketNotification>

    fun findAllByUserId(userId: Long): List<TicketNotification>

    fun deleteAllByTicketIdInAndUserId(ticketId: List<Long>, userId: Long)
}