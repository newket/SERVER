package com.newket.infra.jpa.ticket.entity

import com.newket.infra.jpa.config.BaseDateEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne
import java.time.LocalDate
import java.time.LocalTime

@Entity
class TicketSaleSchedule(
    @ManyToOne(fetch = FetchType.LAZY)
    val ticketSaleUrl: TicketSaleUrl,
    val day: LocalDate,
    val time: LocalTime,
    val type: String
) : BaseDateEntity()