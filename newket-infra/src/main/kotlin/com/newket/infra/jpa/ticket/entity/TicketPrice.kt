package com.newket.infra.jpa.ticket.entity

import com.newket.infra.jpa.config.BaseDateEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne

@Entity
class TicketPrice(
    @ManyToOne(fetch = FetchType.LAZY)
    val ticket: Ticket,
    val type: String,
    val price: String,
) : BaseDateEntity()