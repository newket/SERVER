package com.newket.infra.jpa.ticket.entity

import com.newket.infra.jpa.config.BaseDateEntity
import com.newket.infra.jpa.ticket.constant.TicketProvider
import jakarta.persistence.*

@Entity
class TicketSaleUrl(
    @ManyToOne(fetch = FetchType.LAZY)
    val ticket: Ticket,
    @Enumerated(EnumType.STRING)
    val ticketProvider: TicketProvider,
    val url: String,
    val isDirectUrl: Boolean
) : BaseDateEntity()