package com.newket.infra.jpa.ticket.repository

import com.newket.infra.jpa.ticket.entity.TicketPrice
import org.springframework.data.jpa.repository.JpaRepository

interface TicketPriceRepository : JpaRepository<TicketPrice, Long> {
    fun findAllByTicketId(ticketId: Long): List<TicketPrice>
}