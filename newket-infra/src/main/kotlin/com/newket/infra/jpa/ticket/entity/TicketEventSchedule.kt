package com.newket.infra.jpa.ticket.entity

import com.newket.infra.jpa.config.BaseDateEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne
import java.time.LocalDate
import java.time.LocalTime

@Entity
class TicketEventSchedule(
    @ManyToOne(fetch = FetchType.LAZY)
    val ticket: Ticket,
    val day: LocalDate,
    val time: LocalTime
) : BaseDateEntity()