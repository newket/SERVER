package com.newket.infra.jpa.notifiacation.repository

import com.newket.infra.jpa.notifiacation.entity.TicketNotification
import org.springframework.data.jpa.repository.JpaRepository

interface TicketNotificationRepository : JpaRepository<TicketNotification, Long> {
    fun findByTicketIdAndUserId(ticketId: Long, userId: Long): TicketNotification?

    fun deleteByTicketIdAndUserId(ticketId: Long, userId: Long)

    fun findAllByTicketId(ticketId: Long): List<TicketNotification>

    fun deleteAllByTicketIdInAndUserId(ticketId: List<Long>, userId: Long)
}