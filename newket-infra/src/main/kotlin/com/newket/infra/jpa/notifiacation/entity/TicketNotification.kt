package com.newket.infra.jpa.notifiacation.entity

import com.newket.infra.jpa.config.BaseDateEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "ticket_notification",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "ticket_id"]) // 복합 유니크 제약
    ]
)
class TicketNotification(
    val userId: Long,
    val ticketId: Long
) : BaseDateEntity()