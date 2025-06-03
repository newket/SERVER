package com.newket.infra.jpa.ticket_artist.entity

import com.newket.infra.jpa.config.BaseDateEntity
import com.newket.infra.jpa.ticket.entity.Ticket
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne

@Entity
class TicketLineup (
    @ManyToOne(fetch = FetchType.LAZY)
    val ticket: Ticket,
    val imageUrl: String,
) : BaseDateEntity()